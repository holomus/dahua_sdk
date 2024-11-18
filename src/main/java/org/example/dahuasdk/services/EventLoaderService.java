package org.example.dahuasdk.services;

import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Memory;
import lombok.RequiredArgsConstructor;
import org.example.dahuasdk.dto.EventDTO;
import org.example.dahuasdk.exceptions.EventLoadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EventLoaderService {
    private static final Logger log = LoggerFactory.getLogger(EventLoaderService.class);
    private final EventProcessorService eventProcessor;

    public List<EventDTO> findAccessRecords(
            NetSDKLib netSDKInstance,
            NetSDKLib.LLong loginHandle,
            String deviceId,
            LocalDateTime eventStartLoadTime,
            LocalDateTime eventEndLoadTime
    ) {
        if (eventStartLoadTime == null || eventEndLoadTime == null) return new ArrayList<>();

        final int loadWaitTimeMilliseconds = 5000;
        List<EventDTO> eventDTOList = new ArrayList<>();

        NetSDKLib.NET_OUT_FIND_RECORD_PARAM stuFindOutParam = null;

        try {
            NetSDKLib.FIND_RECORD_ACCESSCTLCARDREC_CONDITION_EX recordCondition = new NetSDKLib.FIND_RECORD_ACCESSCTLCARDREC_CONDITION_EX();
            recordCondition.bTimeEnable = 1;
            recordCondition.stStartTime = new NetSDKLib.NET_TIME();
            recordCondition.stEndTime = new NetSDKLib.NET_TIME();

            recordCondition.stStartTime.setTime(
                eventStartLoadTime.getYear(),
                eventStartLoadTime.getMonthValue(),
                eventStartLoadTime.getDayOfMonth(),
                eventStartLoadTime.getHour(),
                eventStartLoadTime.getMinute(),
                eventStartLoadTime.getSecond()
            );

            recordCondition.stEndTime.setTime(
                eventEndLoadTime.getYear(),
                eventEndLoadTime.getMonthValue(),
                eventEndLoadTime.getDayOfMonth(),
                eventEndLoadTime.getHour(),
                eventEndLoadTime.getMinute(),
                eventEndLoadTime.getSecond()
            );

            NetSDKLib.NET_IN_FIND_RECORD_PARAM stuFindInParam = new NetSDKLib.NET_IN_FIND_RECORD_PARAM();
            stuFindInParam.emType = NetSDKLib.EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARDREC_EX;
            stuFindInParam.pQueryCondition = recordCondition.getPointer();

            stuFindOutParam = new NetSDKLib.NET_OUT_FIND_RECORD_PARAM();
            recordCondition.write();

            if (!netSDKInstance.CLIENT_FindRecord(loginHandle, stuFindInParam, stuFindOutParam, loadWaitTimeMilliseconds)) {
                throw new EventLoadingException("Cannot Find Record. Error Code: " + String.format("0x%x", netSDKInstance.CLIENT_GetLastError()));
            }

            log.debug("FindRecord Succeeded\nFindHandle: {}", stuFindOutParam.lFindeHandle);

            recordCondition.read();
            int count = 0;
            final int queryPageSize = 100;

            NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[] pstRecord = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[queryPageSize];
            for (int i = 0; i < queryPageSize; i++) {
                pstRecord[i] = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC();
            }

            NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM stuFindNextInParam = new NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM();
            stuFindNextInParam.lFindeHandle = stuFindOutParam.lFindeHandle;
            stuFindNextInParam.nFileCount = queryPageSize;

            NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM stuFindNextOutParam = new NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM();
            stuFindNextOutParam.nMaxRecordNum = queryPageSize;
            stuFindNextOutParam.pRecordList = new Memory((long) pstRecord[0].dwSize * queryPageSize);
            stuFindNextOutParam.pRecordList.clear((long) pstRecord[0].dwSize * queryPageSize);
            ToolKits.SetStructArrToPointerData(pstRecord, stuFindNextOutParam.pRecordList);

            log.debug("Event load start for device {}", deviceId);
            while (true) {
                if (!netSDKInstance.CLIENT_FindNextRecord(stuFindNextInParam, stuFindNextOutParam, loadWaitTimeMilliseconds)) {
                    throw new EventLoadingException("FindNextRecord Failed. Error Code: " + netSDKInstance.CLIENT_GetLastError());
                }

                ToolKits.GetPointerDataToStructArr(stuFindNextOutParam.pRecordList, pstRecord);

                for (int i = 0; i < stuFindNextOutParam.nRetRecordNum; i++) {
                    var event = new EventDTO(pstRecord[i], deviceId);
                    eventDTOList.add(event);

                    log.debug("Loaded event for deviceID: {}, eventTime: {}, personCode: {}, eventCode: {}", event.getDeviceId(), event.getUTCEventTime(), event.getPersonCode(), event.getEventCode());
                }

                if (stuFindNextOutParam.nRetRecordNum < queryPageSize) {
                    break;
                } else {
                    count++;
                }
            }
        }
        catch (EventLoadingException ex) {
            log.error(ex.getMessage());
        }
        finally {
            if (stuFindOutParam != null) {
                netSDKInstance.CLIENT_FindRecordClose(stuFindOutParam.lFindeHandle);
            }
        }
        log.debug("Event load end for device {}", deviceId);

        return eventDTOList;
    }

    public void loadAccessRecords(
        NetSDKLib netSDKInstance,
        NetSDKLib.LLong loginHandle,
        String deviceId,
        LocalDateTime eventStartLoadTime,
        LocalDateTime eventEndLoadTime
    ) {
        try {
            if (eventStartLoadTime == null || eventEndLoadTime == null) return;
            log.debug("load events for {} between {} and {}", deviceId, eventStartLoadTime, eventEndLoadTime);
            var events = findAccessRecords(netSDKInstance, loginHandle, deviceId, eventStartLoadTime, eventEndLoadTime);
            eventProcessor.processEvents(events, deviceId);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }
}
