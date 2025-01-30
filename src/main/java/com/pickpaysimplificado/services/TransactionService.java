package com.pickpaysimplificado.services;

import com.pickpaysimplificado.domain.transaction.Transaction;
import com.pickpaysimplificado.domain.user.User;
import com.pickpaysimplificado.dtos.TransactionDTO;
import com.pickpaysimplificado.reposotories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class TransactionService {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    public void createTransaction(TransactionDTO transaction) throws Exception {
        User sender  = this.userService.findUserById(transaction.senderId());
        User reciver = this.userService.findUserById(transaction.reciverId());

        userService.validateTransaction(sender, transaction.value());

        boolean isAuthorazed = this.authorizeTransaction(sender, transaction.value());
        if(!isAuthorazed){ throw new Exception("Transação não autorizada"); }

        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(transaction.value());
        newTransaction.setSender(sender);
        newTransaction.setReciver(reciver);
        newTransaction.setTimestamp(LocalDateTime.now());

        sender.setBalance(sender.getBalance().subtract(transaction.value()));
        reciver.setBalance(reciver.getBalance().add(transaction.value()));

        this.repository.save(newTransaction);
        this.userService.saveUser(sender);
        this.userService.saveUser(reciver);
    }

    public boolean authorizeTransaction(User sender, BigDecimal value){
        ResponseEntity<Map> authorizationResponse = restTemplate.getForEntity("https://util.devi.tools/api/v2/authorize", Map.class);

        if(authorizationResponse.getStatusCode() == HttpStatus.OK){
            String message = (String) authorizationResponse.getBody().get("authorization");
            return "Autorizado".equalsIgnoreCase(message);
        }
        else return false;
    }

}
