package org.example.dahuasdk.services;

import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Memory;
import lombok.RequiredArgsConstructor;
import org.example.dahuasdk.client.vhr.entity.load.PersonDTO;
import org.example.dahuasdk.client.vhr.entity.load.PersonFaceUpdateDto;
import org.example.dahuasdk.dto.PersonInfoDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor

public class PersonService {
    final private NetSDKLib netsdk = NetSDKLib.NETSDK_INSTANCE;


    private List<Integer> getFailCodes(NetSDKLib.FAIL_CODE[] failCodes) {
        List<Integer> result = new ArrayList<>(failCodes.length);

        for (int i = 0; i < failCodes.length; i ++) {
            result.set(i, failCodes[i].nFailCode);
        }

        return result;
    }


    public List<Integer> savePerson(
            List<PersonDTO> personInsertInfos,
            NetSDKLib.LLong loginHandle
    ) throws Exception {
        int emtype = NetSDKLib.NET_EM_ACCESS_CTL_USER_SERVICE.NET_EM_ACCESS_CTL_USER_SERVICE_INSERT;
        int nMaxNum = personInsertInfos.size();
        NetSDKLib.NET_ACCESS_USER_INFO[] persons = new NetSDKLib.NET_ACCESS_USER_INFO[nMaxNum];

        Calendar calendar = new Calendar.Builder().build();

        for (int i = 0; i < nMaxNum; i++) {
            NetSDKLib.NET_ACCESS_USER_INFO personInfo = new NetSDKLib.NET_ACCESS_USER_INFO();
            PersonDTO personInsertInfo = personInsertInfos.get(i);

            System.arraycopy(personInsertInfo.getUserId().getBytes(), 0, personInfo.szUserID, 0,
                    personInsertInfo.getUserId().getBytes().length);
            personInfo.szName = new byte[100];
            System.arraycopy(personInsertInfo.getName().getBytes(), 0, personInfo.szName, 0,
                    personInsertInfo.getName().getBytes().length);
            personInfo.emUserType = personInsertInfo.getUserType();
            personInfo.nUserStatus = personInsertInfo.getUserStatus();

            personInfo.stuValidBeginTime = new NetSDKLib.NET_TIME();
            calendar.setTime(personInsertInfo.getStuValidBeginTime());
            personInfo.stuValidBeginTime.setTime(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND));

            calendar.setTime(personInsertInfo.getStuValidEndTime());
            personInfo.stuValidEndTime = new NetSDKLib.NET_TIME();
            personInfo.stuValidEndTime.setTime(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND));

            Memory memory = new Memory(personInfo.size());
            personInfo.write();
            memory.write(0, personInfo.getPointer().getByteArray(0, personInfo.size()), 0, personInfo.size());

            persons[i] = personInfo;
        }

        NetSDKLib.NET_IN_ACCESS_USER_SERVICE_INSERT inParam = new NetSDKLib.NET_IN_ACCESS_USER_SERVICE_INSERT();
        inParam.nInfoNum = 1;
        inParam.pUserInfo = new Memory((long) persons[0].size() * nMaxNum);
        inParam.pUserInfo.clear((long) persons[0].size() * nMaxNum);

        NetSDKLib.NET_OUT_ACCESS_USER_SERVICE_INSERT outParam = new NetSDKLib.NET_OUT_ACCESS_USER_SERVICE_INSERT();
        outParam.nMaxRetNum = 1;

        NetSDKLib.FAIL_CODE[] failCodes = new NetSDKLib.FAIL_CODE[nMaxNum];

        for (int i = 0; i < nMaxNum; i++) {
            failCodes[i] = new NetSDKLib.FAIL_CODE();
        }

        outParam.pFailCode = new Memory((long) failCodes[0].size() * nMaxNum);
        outParam.pFailCode.clear((long) failCodes[0].size() * nMaxNum);

        ToolKits.SetStructArrToPointerData(persons, inParam.pUserInfo);
        ToolKits.SetStructArrToPointerData(failCodes, outParam.pFailCode);

        inParam.write();
        outParam.write();

        boolean result = netsdk.CLIENT_OperateAccessUserService(
                loginHandle,
                emtype,
                inParam.getPointer(),
                outParam.getPointer(),
                3000
        );

        if (result) {
            System.out.println("users added successfully.");
            ToolKits.GetPointerDataToStructArr(outParam.pFailCode, failCodes);
        } else {
            throw new Exception("users added failed");
        }

        return getFailCodes(failCodes);
    }

    public List<Integer> removePerson(
            String[] userIds,
            NetSDKLib.LLong loginHandle
    ) throws Exception {
        int nMaxNum = userIds.length;
        int emtype = NetSDKLib.NET_EM_ACCESS_CTL_USER_SERVICE.NET_EM_ACCESS_CTL_USER_SERVICE_REMOVE;
        NetSDKLib.FAIL_CODE[] failCodes = new NetSDKLib.FAIL_CODE[nMaxNum];

        for (int i = 0; i < nMaxNum; i++) {
            failCodes[i] = new NetSDKLib.FAIL_CODE();
        }

        NetSDKLib.NET_IN_ACCESS_USER_SERVICE_REMOVE stIn = new NetSDKLib.NET_IN_ACCESS_USER_SERVICE_REMOVE();
        stIn.nUserNum = userIds.length;

        for (int i = 0; i < userIds.length; i++) {
            System.arraycopy(userIds[i].getBytes(),
                    0,
                    stIn.szUserIDs[i].szUserID,
                    0,
                    userIds[i].getBytes().length);
        }

        NetSDKLib.NET_OUT_ACCESS_USER_SERVICE_REMOVE stOut = new NetSDKLib.NET_OUT_ACCESS_USER_SERVICE_REMOVE();
        stOut.nMaxRetNum = nMaxNum;
        stOut.pFailCode = new Memory((long) failCodes[0].size() * nMaxNum);
        stOut.pFailCode.clear((long) failCodes[0].size() * nMaxNum);

        ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);

        stIn.write();
        stOut.write();

        System.out.println("(removePerson) loginHandle = " + loginHandle);

        boolean result = netsdk.CLIENT_OperateAccessUserService(
                loginHandle,
                emtype,
                stIn.getPointer(),
                stOut.getPointer(),
                3000
        );

        if (result) {
            ToolKits.GetPointerDataToStructArr(stOut.pFailCode, failCodes);
        } else {
            throw new Exception("Failed to remove user. error_message = " + ToolKits.getErrorCodeShow());
        }

        return getFailCodes(failCodes);
    }

//    public List<PersonInfoDto> getPerson(
//            String[] userIDs,
//            AutoRegisterService autoRegisterService
//    )  {
//        NetSDKLib.LLong loginHandle = autoRegisterService.login(
//                autoRegisterService.gpIp,
//                autoRegisterService.gwPort,
//                "admin",
//                "verifix1234",
//                autoRegisterService.gfinalDeviceId
//        );
//
//        int nMaxNum = userIDs.length;
//        int emtype = NetSDKLib.NET_EM_ACCESS_CTL_USER_SERVICE.NET_EM_ACCESS_CTL_USER_SERVICE_GET;
//
//        NetSDKLib.NET_ACCESS_USER_INFO[] users = new NetSDKLib.NET_ACCESS_USER_INFO[nMaxNum];
//        NetSDKLib.FAIL_CODE[] failCodes = new NetSDKLib.FAIL_CODE[nMaxNum];
//
//        for (int i = 0; i < nMaxNum; i++) {
//            users[i] = new NetSDKLib.NET_ACCESS_USER_INFO();
//            failCodes[i] = new NetSDKLib.FAIL_CODE();
//        }
//
//        NetSDKLib.NET_IN_ACCESS_USER_SERVICE_GET stIn = new NetSDKLib.NET_IN_ACCESS_USER_SERVICE_GET();
//        stIn.nUserNum = userIDs.length;
//
//        for (int i = 0; i < userIDs.length; i++) {
//            System.arraycopy(userIDs[i].getBytes(),
//                    0,
//                    stIn.szUserIDs[i].szUserID,
//                    0,
//                    userIDs[i].getBytes().length);
//        }
//
//        NetSDKLib.NET_OUT_ACCESS_USER_SERVICE_GET stOut = new NetSDKLib.NET_OUT_ACCESS_USER_SERVICE_GET();
//        stOut.nMaxRetNum = nMaxNum;
//        stOut.pUserInfo = new Memory((long) users[0].size() * nMaxNum);
//        stOut.pUserInfo.clear((long) users[0].size() * nMaxNum);
//        stOut.pFailCode = new Memory((long) failCodes[0].size() * nMaxNum);
//        stOut.pFailCode.clear((long) failCodes[0].size() * nMaxNum);
//
//        ToolKits.SetStructArrToPointerData(users, stOut.pUserInfo);
//        ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);
//
//        stIn.write();
//        stOut.write();
//
//        System.out.println("(removePerson) loginHandle = " + loginHandle);
//
//
//        boolean result = netsdk.CLIENT_OperateAccessUserService(
//                loginHandle,
//                emtype,
//                stIn.getPointer(),
//                stOut.getPointer(),
//                3000
//        );
//
//        autoRegisterService.logout(loginHandle);
//
//        List<PersonInfoDto> personInfoDtos = new ArrayList<>();
//
//        if (result) {
//            ToolKits.GetPointerDataToStructArr(stOut.pUserInfo, users);
//            ToolKits.GetPointerDataToStructArr(stOut.pFailCode, failCodes);
//
//            for (int i = 0; i < nMaxNum; i++) {
//                NetSDKLib.NET_ACCESS_USER_INFO info = users[i];
//                PersonInfoDto personInfoDto = new PersonInfoDto();
//
//                personInfoDto.setUserId(new String(info.szUserID, java.nio.charset.StandardCharsets.UTF_8));
//                personInfoDto.setName(new String(info.szName, java.nio.charset.StandardCharsets.UTF_8));
//                personInfoDto.setUserType(info.emUserType);
//                personInfoDto.setUserStatus(info.nUserStatus);
//                personInfoDto.setStuValidBeginTime(convertToDate(info.stuValidBeginTime));
//                personInfoDto.setStuValidEndTime(convertToDate(info.stuValidEndTime));
//
//                personInfoDtos.add(personInfoDto);
//            }
//        } else {
//            System.err.println("Failed to get user.");
//            System.err.println("Failed to search for the users, " + ToolKits.getErrorCodeShow());
//        }
//
//        return personInfoDtos;
//    }
//

    public List<Integer> savePersonFace(
            List<PersonFaceUpdateDto> personFaceUpdateDtos,
            NetSDKLib.LLong loginHandle
    ) {
        PersonFaceUpdateDto personFaceDto;
        int emtype = NetSDKLib.NET_EM_ACCESS_CTL_FACE_SERVICE.NET_EM_ACCESS_CTL_FACE_SERVICE_UPDATE;
        int nMaxNum = personFaceUpdateDtos.size();

        NetSDKLib.NET_ACCESS_FACE_INFO[] faceInfos = new NetSDKLib.NET_ACCESS_FACE_INFO[nMaxNum];
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < personFaceUpdateDtos.size(); i++) {
            NetSDKLib.NET_ACCESS_FACE_INFO faceInfo = new NetSDKLib.NET_ACCESS_FACE_INFO();
            personFaceDto = personFaceUpdateDtos.get(i);

            String userId = personFaceDto.getUserId();
            List<byte[]> imageDatas =  List.of(personFaceDto.getFaceImage());
            int nFacePhoto = imageDatas.size();

            faceInfo.nFacePhoto = nFacePhoto;

            System.arraycopy(
                    userId.getBytes(),
                    0,
                    faceInfo.szUserID,
                    0, Math.min(userId.length(), faceInfo.szUserID.length)
            );

            for (int j = 0; j < nFacePhoto; j++) {
                faceInfo.nInFacePhotoLen[j] = 200 * 1024;
                faceInfo.nOutFacePhotoLen[j] = imageDatas.get(j).length;
                faceInfo.pFacePhotos[j].pFacePhoto = new Memory(200 * 1024);
                faceInfo.pFacePhotos[j].pFacePhoto.clear(200 * 1024);
                faceInfo.pFacePhotos[j].pFacePhoto.write(0, imageDatas.get(j), 0, imageDatas.get(j).length);
            }

            faceInfo.stuUpdateTime.setTime(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND)
            );

            faceInfos[i] = faceInfo;
        }

        NetSDKLib.NET_IN_ACCESS_FACE_SERVICE_INSERT faceInsert = new NetSDKLib.NET_IN_ACCESS_FACE_SERVICE_INSERT();
        faceInsert.nFaceInfoNum = nMaxNum;

        faceInsert.pFaceInfo = new Memory((long) faceInfos[0].size() * nMaxNum);
        faceInsert.pFaceInfo.clear((long) faceInfos[0].size() * nMaxNum);

        NetSDKLib.NET_OUT_ACCESS_FACE_SERVICE_INSERT faceInsertOut = new NetSDKLib.NET_OUT_ACCESS_FACE_SERVICE_INSERT();
        faceInsertOut.nMaxRetNum = nMaxNum;

        NetSDKLib.FAIL_CODE[] failCodes = new NetSDKLib.FAIL_CODE[nMaxNum];

        for (int i = 0; i < nMaxNum; i++) {
            failCodes[i] = new NetSDKLib.FAIL_CODE();
        }

        faceInsertOut.pFailCode = new Memory((long) failCodes[0].size() * nMaxNum);
        faceInsertOut.pFailCode.clear((long) failCodes[0].size() * nMaxNum);

        ToolKits.SetStructArrToPointerData(faceInfos, faceInsert.pFaceInfo);
        ToolKits.SetStructArrToPointerData(failCodes, faceInsertOut.pFailCode);

        faceInsert.write();
        faceInsertOut.write();

        boolean ret = netsdk.CLIENT_OperateAccessFaceService(
                loginHandle,
                emtype,
                faceInsert.getPointer(),
                faceInsertOut.getPointer(),
                5000
        );

        ToolKits.GetPointerDataToStructArr(faceInsertOut.pFailCode, failCodes);

        if (!ret) {
            System.out.println("Add face failed: " + ToolKits.getErrorCodePrint());
        }

        System.out.println("Add face succeeded");

        return getFailCodes(failCodes);
    }

    public boolean removeFace(
            String[] userIDs,
            NetSDKLib.LLong loginHandle
    ) {
        int nMaxNum = userIDs.length;
        int emtype = NetSDKLib.NET_EM_ACCESS_CTL_FACE_SERVICE.NET_EM_ACCESS_CTL_FACE_SERVICE_REMOVE;
        NetSDKLib.FAIL_CODE[] failCodes = new NetSDKLib.FAIL_CODE[nMaxNum];

        for (int i = 0; i < nMaxNum; i++) {
            failCodes[i] = new NetSDKLib.FAIL_CODE();
        }

        NetSDKLib.NET_IN_ACCESS_FACE_SERVICE_REMOVE stIn = new NetSDKLib.NET_IN_ACCESS_FACE_SERVICE_REMOVE();
        stIn.nUserNum = userIDs.length;

        for (int i = 0; i < userIDs.length; i++) {
            System.arraycopy(userIDs[i].getBytes(),
                    0,
                    stIn.szUserIDs[i].szUserID,
                    0,
                    userIDs[i].getBytes().length);
        }


        NetSDKLib.NET_OUT_ACCESS_FACE_SERVICE_REMOVE stOut = new NetSDKLib.NET_OUT_ACCESS_FACE_SERVICE_REMOVE();
        stOut.nMaxRetNum = nMaxNum;
        stOut.pFailCode = new Memory((long) failCodes[0].size() * nMaxNum);
        stOut.pFailCode.clear((long) failCodes[0].size() * nMaxNum);

        ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);

        stIn.write();
        stOut.write();

        boolean ret = netsdk.CLIENT_OperateAccessFaceService(
                loginHandle,
                emtype,
                stIn.getPointer(),
                stOut.getPointer(),
                3000
        );

        if (!ret) {
            System.out.println("Remove face failed: " + ToolKits.getErrorCodePrint());

            return false;
        } else {
            System.out.println("Remove face succeeded");
            return true;
        }
    }

    public static Date convertToDate(NetSDKLib.NET_TIME netTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, netTime.dwYear);
        calendar.set(Calendar.MONTH, netTime.dwMonth); // Oy 0 dan boshlanadi
        calendar.set(Calendar.DAY_OF_MONTH, netTime.dwDay);
        calendar.set(Calendar.HOUR_OF_DAY, netTime.dwHour);
        calendar.set(Calendar.MINUTE, netTime.dwMinute);
        calendar.set(Calendar.SECOND, netTime.dwSecond);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}
