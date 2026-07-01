package br.uel.sistemabiblioteca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aluno")
@Getter
@Setter
@NoArgsConstructor
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    // RA era o identificador do aluno
    @Column(name = "ra", nullable = false, unique = true)
    private String ra;

    // Status de debito
    @Column(nullable = false)
    private boolean possuiDebito = false;

    public Aluno(String nome, String ra) {
        this.nome = nome;
        this.ra = ra;
    }

    // Seria o VerificaAluno do código original
    // Aqui só confere o RA
    public boolean possuiRaValido() {
        return this.ra != null && !this.ra.isBlank();
    }
}
