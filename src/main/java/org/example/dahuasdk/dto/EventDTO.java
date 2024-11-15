package org.example.dahuasdk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.netsdk.lib.NetSDKLib;
import lombok.*;
import org.example.dahuasdk.serializers.UnixTimestampSerializer;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class EventDTO {
    private String deviceId;
    @JsonSerialize(using = UnixTimestampSerializer.class)
    private LocalDateTime UTCEventTime;
    private int eventCode;
    private String personCode;
    private String cardNo;

    public EventDTO(NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC cardRecord, String deviceId) {
        this.deviceId = deviceId;
        this.UTCEventTime = LocalDateTime.of(
                cardRecord.stuTime.dwYear,
                cardRecord.stuTime.dwMonth,
                cardRecord.stuTime.dwDay,
                cardRecord.stuTime.dwHour,
                cardRecord.stuTime.dwMinute,
                cardRecord.stuTime.dwSecond
        );
        this.eventCode = cardRecord.nErrorCode;
        this.personCode = new String(cardRecord.szUserID).trim();
        this.cardNo = new String(cardRecord.szCardNo).trim();
    }

    public EventDTO(NetSDKLib.DEV_EVENT_ACCESS_CTL_INFO event, String deviceId) {
        this.deviceId = deviceId;
        this.UTCEventTime = LocalDateTime.of(
                event.UTC.dwYear,
                event.UTC.dwMonth,
                event.UTC.dwDay,
                event.UTC.dwHour,
                event.UTC.dwMinute,
                event.UTC.dwSecond
        );
        this.eventCode = event.nErrorCode;
        this.personCode = new String(event.szUserID).trim();
        this.cardNo = new String(event.szCardNo).trim();
    }
}
