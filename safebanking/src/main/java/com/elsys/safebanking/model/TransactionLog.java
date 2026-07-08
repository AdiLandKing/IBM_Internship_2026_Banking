package com.elsys.safebanking.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "transaction_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logID")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tranID", nullable = false)
    private BankingTransaction transaction;

    @Column(name = "log_entry_text", nullable = false, columnDefinition = "TEXT")
    private String logEntryText;

    @Column(name = "time_stamp", nullable = false)
    private Instant timeStamp;
}