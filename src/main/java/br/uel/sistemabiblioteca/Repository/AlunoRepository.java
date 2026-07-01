package br.uel.sistemabiblioteca.repository;

import br.uel.sistemabiblioteca.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Optional<Aluno> findByRa(String ra);
}
