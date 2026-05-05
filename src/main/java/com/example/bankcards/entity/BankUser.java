package com.example.bankcards.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Table(name = "bank_user")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String username;
    private String password;
    private String email;
    private String name;

    private Boolean blocked = false;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Card> cards;

    @ManyToOne
    @JoinColumn(name="role_id")
    private UserRole userRole;

}
