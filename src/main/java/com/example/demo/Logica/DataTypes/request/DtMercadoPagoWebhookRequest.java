package com.example.demo.Logica.DataTypes.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtMercadoPagoWebhookRequest {
    private String id;
    private String type;
    private String topic;
    private DtMercadoPagoWebhookData data;
}
