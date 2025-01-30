package com.pickpaysimplificado.dtos;

import java.math.BigDecimal;

public record TransactionDTO(BigDecimal value, Long senderId, Long reciverId) {
}
