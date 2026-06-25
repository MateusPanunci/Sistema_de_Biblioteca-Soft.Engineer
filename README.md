# Sistema de Biblioteca

Projeto desenvolvido para a disciplina de Engenharia de Software da Universidade Estadual de Londrina (UEL).

## Objetivo

O Sistema de Biblioteca tem como finalidade gerenciar livros, alunos e operações de empréstimo e devolução, aplicando conceitos de Engenharia de Software, Desenvolvimento Ágil e DevOps.

## Funcionalidades

### Cadastro
- Cadastro de livros
- Cadastro de alunos
- Consulta de registros

### Empréstimos
- Realizar empréstimo de livros
- Verificar disponibilidade
- Registrar data de empréstimo

### Devoluções
- Registrar devolução de livros
- Atualizar disponibilidade
- Registrar histórico de devolução

## Arquitetura

O sistema está sendo desenvolvido utilizando uma **Arquitetura em Camadas**, integrando os padrões **MVC (Model-View-Controller)**, **Service Layer** e **DAO (Data Access Object)** para comunicação via protocolo HTTP.

O fluxo de dados da aplicação respeita a seguinte divisão estrutural:

1. **Camada de Apresentação (View):** Interface do usuário responsável por capturar as interações e disparar requisições HTTP para o backend.
2. **Camada de Controle (Controller):** Atua como o ponto de entrada das requisições HTTP. Sua função é receber os dados da View, realizar validações sintáticas básicas de entrada e delegar a execução para a camada de serviços, retornando a resposta HTTP correspondente.
3. **Camada de Serviço (Service Layer):** Centraliza toda a lógica e as regras de negócio do sistema (ex: verificar a disponibilidade de um livro antes de efetivar o empréstimo). É a camada que orquestra as regras do domínio e garante a integridade das operações.
4. **Camada de Persistência (DAO):** Isolada e especializada no acesso ao Banco de Dados (SGBD). É acionada exclusivamente pela Camada de Serviço para persistir ou consultar as entidades (como `Aluno`, `Livro`, `Emprestimo`).

**Justificativa da Escolha:**
- A introdução da Camada de Serviço faz com que os controladores fiquem responsáveis apenas pelo ciclo de vida do HTTP. Isso resulta em um código com alto desacoplamento e alta coesão.
- Além disso, isolar as regras de negócio na Service Layer facilita drasticamente a escrita de testes unitários automatizados, permitindo que os cenários de teste validem as lógicas de empréstimo e devolução sem a necessidade de simular requisições HTTP ou renderizar interfaces gráficas.
