# Diagrama de Classes

Diagrama de classes do Sistema de Biblioteca atualizado com todas as camadas da
arquitetura: Controle (Controller), Serviço (Service), Persistência (DAO, padrão
Data Access Object via Spring Data JPA) e o modelo de domínio (entidades).

```mermaid
classDiagram
    direction TB

    %% ===== camada de controle =====
    class HomeController {
        <<Controller>>
        +visaoGeral(model) String
    }

    class LivroController {
        <<Controller>>
        +listar(model) String
        +formulario() String
        +cadastrar(titulo, autor, isbn, prazoDevolucaoDias, reservado) String
    }

    class AlunoController {
        <<Controller>>
        +listar(model) String
        +formulario() String
        +cadastrar(nome, ra) String
    }

    class EmprestimoController {
        <<Controller>>
        +formularioEmprestimo(model) String
        +realizarEmprestimo(ra, livroIds) String
    }

    class DevolucaoController {
        <<Controller>>
        +formularioDevolucao(model) String
        +realizarDevolucao(emprestimoId) String
    }

    %% ===== camada de servico =====
    class EmprestimoService {
        <<Service>>
        +emprestar(ra, livroIds) Emprestimo
    }

    class DevolucaoService {
        <<Service>>
        +devolver(emprestimoId) Emprestimo
    }

    %% ===== camada de persistencia (DAO) =====
    class AlunoRepository {
        <<interface DAO>>
        +findByRa(String ra) Optional~Aluno~
        +save(Aluno aluno) Aluno
        +findAll() List~Aluno~
        +count() long
    }

    class LivroRepository {
        <<interface DAO>>
        +findByIsbn(String isbn) Optional~Livro~
        +save(Livro livro) Livro
        +findAll() List~Livro~
    }

    class EmprestimoRepository {
        <<interface DAO>>
        +findByAlunoId(Long alunoId) List~Emprestimo~
        +findByStatus(StatusEmprestimo status) List~Emprestimo~
        +save(Emprestimo emprestimo) Emprestimo
        +findById(Long id) Optional~Emprestimo~
    }

    %% ===== modelo de dominio =====
    class Aluno {
        -Long id
        -String nome
        -String ra
        -boolean possuiDebito
        +possuiRaValido() boolean
    }

    class Livro {
        -Long id
        -String titulo
        -String autor
        -String isbn
        -Integer prazoDevolucaoDias
        -boolean reservado
        -boolean disponivel
        +podeSerEmprestado() boolean
    }

    class Emprestimo {
        -Long id
        -LocalDate dataEmprestimo
        -LocalDate dataPrevistaDevolucao
        -LocalDate dataDevolucaoEfetiva
        -StatusEmprestimo status
    }

    class StatusEmprestimo {
        <<enumeration>>
        ATIVO
        DEVOLVIDO
    }

    %% ===== dependencias: controle -> servico =====
    EmprestimoController --> EmprestimoService : delega caso de uso
    DevolucaoController --> DevolucaoService : delega caso de uso

    %% ===== dependencias: controle -> persistencia (CRUD simples) =====
    LivroController --> LivroRepository : consulta e cadastro
    AlunoController --> AlunoRepository : consulta e cadastro
    HomeController --> LivroRepository : indicadores
    HomeController --> AlunoRepository : indicadores
    HomeController --> EmprestimoRepository : indicadores

    %% ===== dependencias: servico -> persistencia =====
    EmprestimoService --> AlunoRepository : valida aluno
    EmprestimoService --> LivroRepository : valida disponibilidade
    EmprestimoService --> EmprestimoRepository : persiste emprestimo
    DevolucaoService --> EmprestimoRepository : registra devolucao
    DevolucaoService --> AlunoRepository : aplica debito
    DevolucaoService --> LivroRepository : libera livros

    %% ===== persistencia -> dominio =====
    AlunoRepository ..> Aluno : gerencia persistência
    LivroRepository ..> Livro : gerencia persistência
    EmprestimoRepository ..> Emprestimo : gerencia persistência

    %% ===== relacionamentos do dominio =====
    Emprestimo "1" --> "1" Aluno : pertence a
    Emprestimo "*" --> "*" Livro : contém
    Emprestimo --> StatusEmprestimo : possui
```

## Observações

- A camada de persistência aplica o padrão **DAO** por meio de interfaces do Spring
  Data JPA: cada Repository é o objeto de acesso a dados de uma entidade, expondo as
  operações de consulta e escrita e isolando as demais camadas da tecnologia de
  banco de dados.
- Os controllers dos casos de uso (Emprestar e Devolver) dependem apenas dos
  Services, que concentram as regras de negócio. Os controllers de cadastro e
  consulta acessam os Repositories diretamente por se tratarem de operações simples
  de CRUD.
- Os métodos `save`, `findAll`, `findById` e `count` são herdados de
  `JpaRepository` e foram representados nos DAOs por serem utilizados pelo sistema.
