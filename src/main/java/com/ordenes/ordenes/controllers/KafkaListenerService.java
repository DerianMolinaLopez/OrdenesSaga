package com.inventarios.inventarios.controller;

import java.io.IOException;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordenes.ordenes.constants.StringsKafkaConstants;
import com.ordenes.ordenes.controllers.ControllerKafkaPublisher;
import com.ordenes.ordenes.enums.TypeMessage;
import com.ordenes.ordenes.services.WorkOrderService;
import com.ordenes.ordenes.utils.CreateStringStatusResponse;


@Controller
public class KafkaListenerService {
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private ControllerKafkaPublisher controllerKafkaPublisher;
    @Autowired
    private CreateStringStatusResponse createStringStatusResponse;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String topicError = "errores";
    private final String topicConfirmation = "confirmaciones";

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(KafkaListener.class);

   @KafkaListener(topics = StringsKafkaConstants.TOPIC_ORDER, groupId = StringsKafkaConstants.TOPIC_ORDER)
    public void listen(@Payload String message,
            @Header(value = "objetivo", required = false) String objetivo,
            @Header(value = "correlationId", required = false) String numeroOperacion,
            @Header(value = "stepId", required = false) String idStep) {

        logger.info("Mensaje recibido de kafka: {}", message);
        
        if (!"grabado".equals(objetivo)) {
            logger.info("Logica de compesnacion");
            String numberOfOperation = message.split("_")[3];
            this.workOrderService.work();
            return;
        }

      

        processInventoryMessage(message, numeroOperacion, idStep);
    }

    private void processInventoryMessage(String message, String numeroOperacion, String idStep) {
        try {
            JsonNode node = mapper.readTree(message);
      //       workInventoryService.workInventoryLogic(node, idStep);
         
        } catch (IOException | WorkInventoryLogicException e) {
        //    handleProcessingError(e, numeroOperacion, idStep);
        }
    }

    private void handleProcessingError(Exception e, String numeroOperacion, String idStep) {
        logger.error("Ocurrio un error durante la operacion: {}", e.getMessage(), e);
        String messageError = this.createStringStatusResponse.buildResponse(
            TypeMessage.FAILED, 
            numeroOperacion,
            idStep, 
            e.getMessage()
        );
        this.controllerKafkaPublisher.publish(messageError, topicError);
    }

}
