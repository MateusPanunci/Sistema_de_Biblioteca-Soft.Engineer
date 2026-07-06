# Caso de Uso: Devolver Livro

## Descrição do Caso de Uso

| Campo | Descrição |
|---|---|
| **Nome** | Devolver Livro |
| **Ator principal** | Bibliotecário (usuário do sistema) |
| **Objetivo** | Registrar a devolução dos livros de um empréstimo ativo, atualizando a disponibilidade do acervo e a situação do aluno |
| **Pré-condições** | Existir ao menos um empréstimo com status ATIVO no sistema |
| **Pós-condições** | Empréstimo com status DEVOLVIDO e data efetiva registrada; livros do empréstimo disponíveis novamente; débito aplicado ao aluno em caso de atraso |

### Fluxo Principal

1. O bibliotecário acessa a tela de devoluções;
2. O sistema exibe a lista de empréstimos ativos, com aluno, livros, data do
   empréstimo, data prevista de devolução e indicação de atraso;
3. O bibliotecário aciona a devolução do empréstimo desejado;
4. O sistema verifica que o empréstimo está com status ATIVO;
5. O sistema registra a data efetiva da devolução e altera o status do empréstimo
   para DEVOLVIDO;
6. O sistema verifica se a devolução ocorreu dentro do prazo previsto;
7. O sistema marca os livros do empréstimo como disponíveis;
8. O sistema exibe a mensagem de sucesso e atualiza a lista de empréstimos ativos.

### Fluxos Alternativos

**FA1 — Devolução em atraso** (a partir do passo 6 do fluxo principal):

1. O sistema identifica que a data efetiva da devolução é posterior à data prevista;
2. O sistema aplica débito ao aluno (situação "Com débito");
3. O caso de uso retorna ao passo 7 do fluxo principal.

*Observação: enquanto possuir débito, o aluno fica impedido de realizar novos
empréstimos (regra do caso de uso Emprestar Livro).*

### Fluxos de Exceção

**FE1 — Empréstimo inexistente** (no passo 4):

1. O sistema não localiza o empréstimo informado;
2. O sistema exibe a mensagem de erro e encerra o caso de uso.

**FE2 — Empréstimo já devolvido** (no passo 4):

1. O sistema identifica que o empréstimo não está com status ATIVO;
2. O sistema exibe a mensagem de erro informando que o empréstimo já foi devolvido
   e encerra o caso de uso sem alterar nenhum dado.

### Regras de Negócio

- **RN1:** somente empréstimos com status ATIVO podem ser devolvidos;
- **RN2:** a devolução registra a data efetiva do dia em que foi realizada;
- **RN3:** devolução após a data prevista gera débito para o aluno;
- **RN4:** todos os livros do empréstimo voltam a ficar disponíveis após a devolução.

*Os cenários de teste correspondentes a este caso de uso estão descritos em
`docs/cenarios_de_teste.md` (CT-14 a CT-17).*

## Diagrama de Caso de Uso

```mermaid
graph LR
    U([Bibliotecário / Usuário])

    subgraph Sistema de Biblioteca
        DL(Devolver Livro)
        VE(Verificar Empréstimo Ativo)
        RD(Registrar Data de Devolução)
        VA(Verificar Atraso)
        AD(Aplicar Débito ao Aluno)
        LL(Liberar Livros)
    end

    U --> DL
    DL --> VE
    DL --> RD
    DL --> VA
    VA -.->|se em atraso| AD
    DL --> LL
```
