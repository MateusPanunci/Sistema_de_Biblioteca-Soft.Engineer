package br.uel.sistemabiblioteca.repository;

import br.uel.sistemabiblioteca.model.Emprestimo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    List<Emprestimo> findByAlunoId(Long alunoId);

    List<Emprestimo> findByStatus(Emprestimo.StatusEmprestimo status);
}
