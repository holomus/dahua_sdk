package org.example.dahuasdk.services;

import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Memory;
import org.example.dahuasdk.dto.EventDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventLoaderService {
    /// Search for the access control card swiping records by card number
    public static List<EventDTO> findAccessRecord(NetSDKLib netSDKInstance, NetSDKLib.LLong loginHandle, int eventStartLoadUTCTime, int eventEndLoadUTCTime) {
        List<EventDTO> eventDTOList = new ArrayList<>();

        // Search conditions
        NetSDKLib.FIND_RECORD_ACCESSCTLCARDREC_CONDITION_EX recordCondition = new NetSDKLib.FIND_RECORD_ACCESSCTLCARDREC_CONDITION_EX();
        recordCondition.bRealUTCTimeEnable = 1; // Enable searching by UTC TIME
        recordCondition.nStartRealUTCTime = eventStartLoadUTCTime;
        recordCondition.nEndRealUTCTime = eventEndLoadUTCTime;

        // CLIENT_FindRecord Input parameters
        NetSDKLib.NET_IN_FIND_RECORD_PARAM stuFindInParam = new NetSDKLib.NET_IN_FIND_RECORD_PARAM();
        stuFindInParam.emType = NetSDKLib.EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARDREC_EX;
        stuFindInParam.pQueryCondition = recordCondition.getPointer();

        // CLIENT_FindRecord Output parameters
        NetSDKLib.NET_OUT_FIND_RECORD_PARAM stuFindOutParam = new NetSDKLib.NET_OUT_FIND_RECORD_PARAM();
        recordCondition.write();

        if (netSDKInstance.CLIENT_FindRecord(loginHandle, stuFindInParam, stuFindOutParam, 5000)) {
            recordCondition.read();
            System.out.println("FindRecord Succeeded\nFindHandle: " + stuFindOutParam.lFindeHandle);

            int count = 0; // Times of cycles
            final int nRecordCount = 1000; // Number of records for each search

            // Recordset information of access control card swiping records
            NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[] pstRecord = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC[nRecordCount];
            for (int i = 0; i < nRecordCount; i++) {
                pstRecord[i] = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC();
            }

            // CLIENT_FindNextRecord Input parameters
            NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM stuFindNextInParam = new NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM();
            stuFindNextInParam.lFindeHandle = stuFindOutParam.lFindeHandle;
            stuFindNextInParam.nFileCount = nRecordCount; // Number of records for the search

            // CLIENT_FindNextRecord Output parameters
            NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM stuFindNextOutParam = new NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM();
            stuFindNextOutParam.nMaxRecordNum = nRecordCount;
            stuFindNextOutParam.pRecordList = new Memory((long) pstRecord[0].dwSize * nRecordCount);
            stuFindNextOutParam.pRecordList.clear((long) pstRecord[0].dwSize * nRecordCount);
            ToolKits.SetStructArrToPointerData(pstRecord, stuFindNextOutParam.pRecordList); // Copy the array memory to the Pointer

            while (true) { // Cycle search
                if (netSDKInstance.CLIENT_FindNextRecord(stuFindNextInParam, stuFindNextOutParam, 5000)) {
                    ToolKits.GetPointerDataToStructArr(stuFindNextOutParam.pRecordList, pstRecord);

                    for (int i = 0; i < stuFindNextOutParam.nRetRecordNum; i++) {
                        eventDTOList.add(new EventDTO(pstRecord[i], ""));
                    }

                    if (stuFindNextOutParam.nRetRecordNum < nRecordCount) {
                        break;
                    } else {
                        count++;
                    }
                } else {
                    System.err.println("FindNextRecord Failed: " + netSDKInstance.CLIENT_GetLastError());
                    break;
                }
            }

            netSDKInstance.CLIENT_FindRecordClose(stuFindOutParam.lFindeHandle);
        } else {
            System.err.println("Cannot Find This Record. Error Code: " + String.format("0x%x", netSDKInstance.CLIENT_GetLastError()));
        }

        return eventDTOList;
    }
}
