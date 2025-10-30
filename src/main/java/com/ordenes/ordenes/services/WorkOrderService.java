package com.ordenes.ordenes.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordenes.ordenes.controllers.ControllerKafkaPublisher;
import com.ordenes.ordenes.enums.TypeMessage;
import com.ordenes.ordenes.exceptions.WorkOrderException;
import com.ordenes.ordenes.models.ItemEntity;
import com.ordenes.ordenes.repository.ItemEntityRepository;
import com.ordenes.ordenes.utils.CreateStringStatusResponse;

@Service
public class WorkOrderService {
    @Autowired
    private  ItemEntityRepository itemEntityRepository;
    private final Logger logger = LoggerFactory.getLogger(WorkOrderService.class);
    private ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private ControllerKafkaPublisher publisherKafka;
    @Autowired
    private CreateStringStatusResponse createStringStatusResponse;

    private final String topic = "confirmaciones";



    public void work(JsonNode node, String idStep) throws WorkOrderException, IOException {
      String correlationId = node.get("correlationId").asText();
         List<ItemEntity> listOfEntities = this.convertJsonNodeToItemModel(node,correlationId);
         this.logger.info("Debug de cada uno de los items"); 
        
           this.saveAllItems(listOfEntities);
         this.logger.info("Finalizando con la insercion de las compras enviando confirmacion");

         sendMessageConfirm(node.get("correlationId").asText(), idStep);

    }
private List<ItemEntity> convertJsonNodeToItemModel(JsonNode node, String correlationId) throws IOException {
    mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    JsonNode itemsNode = node.get("items");
    List<ItemEntity> nodos = mapper.readerForListOf(ItemEntity.class).readValue(itemsNode);
    LocalDateTime localDate = LocalDateTime.now();
    nodos.forEach(c -> 
    {
       c.setCorrelationId(correlationId);
       c.setLocaldateTime(localDate);
    }
 
    );
    return nodos;
}

    private void saveAllItems(List<ItemEntity >listOfEntities){

         this.itemEntityRepository.saveAll(listOfEntities);
     

    }
    
     private void sendMessageConfirm(String numeroOperacion,String idPaso){
            String exitMessage = this.createStringStatusResponse.buildResponse(TypeMessage.COMPLETED, numeroOperacion, idPaso, "");
            this.publisherKafka.publish(exitMessage, topic);

    }
  

  public void workCompensate(String correlationId){

    //Marcar como cancelada aquellas ordenes que vienen de entrada en la compensacion
    List<ItemEntity> listOfItems = this.getByCorrelationId(correlationId);
    listOfItems.forEach(c-> c.setStatus("CANCELADA"));

    this.saveAllItems(listOfItems);
       

    }

    private List<ItemEntity> getByCorrelationId(String correlationId){

        return this.itemEntityRepository.findByCorrelationIdNative(correlationId);
    }
}
