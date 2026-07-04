```mermaid
erDiagram
    aluno {
        bigint id PK
        varchar nome "NOT NULL"
        varchar ra "NOT NULL, UNIQUE"
        boolean possui_debito "NOT NULL, DEFAULT FALSE"
    }

    livro {
        bigint id PK
        varchar titulo "NOT NULL"
        varchar autor "NOT NULL"
        varchar isbn "UNIQUE"
        integer prazo_devolucao_dias "NOT NULL"
        boolean reservado "NOT NULL, DEFAULT FALSE"
        boolean disponivel "NOT NULL, DEFAULT TRUE"
    }

    emprestimo {
        bigint id PK
        bigint aluno_id FK "NOT NULL"
        date data_emprestimo "NOT NULL, DEFAULT CURRENT_DATE"
        date data_prevista_devolucao
        date data_devolucao_efetiva
        varchar status "NOT NULL, DEFAULT ATIVO"
    }

    emprestimo_livro {
        bigint emprestimo_id PK_FK "NOT NULL"
        bigint livro_id PK_FK "NOT NULL"
    }

    aluno ||--o{ emprestimo : "possui"
    emprestimo ||--|{ emprestimo_livro : "contém"
    livro ||--|{ emprestimo_livro : "contém"
```

> **Nota:** o enum `StatusEmprestimo` (`ATIVO`, `DEVOLVIDO`) do diagrama de classes é mapeado como coluna `status` em `emprestimo`, sem tabela própria.
