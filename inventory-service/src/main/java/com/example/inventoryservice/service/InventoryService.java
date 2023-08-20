package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private  final InventoryRepository inventoryRepository;


    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCodes){
         return inventoryRepository.findBySkuCodeIn(skuCodes).stream()
             .map(inventory ->
               InventoryResponse.builder()
                   .skuCode(inventory.getSkuCode())
                   .isInStock(inventory.getQuantity() > 0)
                   .build()
             ).toList();
    }
}
