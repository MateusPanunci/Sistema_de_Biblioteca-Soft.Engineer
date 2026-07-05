# Cenários de Teste Conceituais

Este documento descreve os cenários de teste conceituais para validar o comportamento
esperado de cada uma das telas projetadas para o Sistema de Biblioteca. Os cenários
servem de base para a criação dos testes automatizados (testes unitários dos Services
e, futuramente, testes das telas).

Cada cenário segue a estrutura: **pré-condição**, **passos** e **resultado esperado**.


## 1. Cadastro de Livros (`/livros/novo`)

### CT-01 — Sucesso ao cadastrar livro
- **Pré-condição:** nenhum livro com o ISBN informado existe no sistema.
- **Passos:** preencher título, autor, ISBN e prazo de devolução (dias) e clicar em "Cadastrar".
- **Resultado esperado:** o livro é salvo no banco, o usuário é redirecionado para a
  listagem de livros com a mensagem de sucesso, e o livro aparece na tabela com o
  status "Disponível".

### CT-02 — Erro ao salvar com campos obrigatórios vazios
- **Pré-condição:** formulário de cadastro de livro aberto.
- **Passos:** deixar título e/ou autor em branco e tentar enviar o formulário.
- **Resultado esperado:** o formulário não é enviado (validação `required` do
  navegador) e o campo vazio é destacado. Caso a requisição chegue ao servidor sem
  os campos, o cadastro é rejeitado com mensagem de erro.

### CT-03 — Erro ao cadastrar ISBN duplicado
- **Pré-condição:** já existe um livro cadastrado com o ISBN "978-85-430-2497-4".
- **Passos:** tentar cadastrar outro livro informando o mesmo ISBN.
- **Resultado esperado:** o cadastro é rejeitado e a mensagem de erro informa que o
  ISBN já está cadastrado. Nenhum registro novo é criado.

### CT-04 — Livro cadastrado como reservado
- **Pré-condição:** formulário de cadastro de livro aberto.
- **Passos:** preencher os dados, marcar a opção "Exemplar reservado" e cadastrar.
- **Resultado esperado:** o livro aparece na listagem com o status "Reservado" e
  **não** aparece na lista de livros disponíveis da tela de empréstimo.

---

## 2. Cadastro de Alunos (`/alunos/novo`)

### CT-05 — Sucesso ao cadastrar aluno
- **Pré-condição:** nenhum aluno com o RA informado existe no sistema.
- **Passos:** preencher nome e RA e clicar em "Cadastrar".
- **Resultado esperado:** o aluno é salvo, o usuário é redirecionado para a listagem
  com mensagem de sucesso, e o aluno aparece na tabela com a situação "Regular".

### CT-06 — Erro ao salvar com campos obrigatórios vazios
- **Pré-condição:** formulário de cadastro de aluno aberto.
- **Passos:** deixar nome e/ou RA em branco e tentar enviar.
- **Resultado esperado:** o formulário não é enviado (validação `required`). Caso a
  requisição chegue ao servidor com RA vazio, o cadastro é rejeitado com mensagem de
  erro de RA inválido (regra `possuiRaValido()` da entidade).

### CT-07 — Erro ao cadastrar RA duplicado
- **Pré-condição:** já existe um aluno cadastrado com o RA "202600123".
- **Passos:** tentar cadastrar outro aluno com o mesmo RA.
- **Resultado esperado:** o cadastro é rejeitado com mensagem informando que o RA já
  existe. Nenhum registro novo é criado.

---

## 3. Empréstimo de Livros (`/emprestimos/novo`)

### CT-08 — Sucesso no empréstimo de um livro
- **Pré-condição:** aluno cadastrado sem débito; livro disponível (não reservado).
- **Passos:** informar o RA do aluno, selecionar um livro e confirmar o empréstimo.
- **Resultado esperado:** o empréstimo é criado com status ATIVO, a data prevista de
  devolução é a data atual somada ao prazo do livro, a mensagem de sucesso exibe a
  data prevista e o livro passa a "Emprestado" (some da lista de disponíveis).

### CT-09 — Sucesso no empréstimo de múltiplos livros
- **Pré-condição:** aluno sem débito; três ou mais livros disponíveis com prazos
  diferentes.
- **Passos:** informar o RA e selecionar 3 livros.
- **Resultado esperado:** a data prevista considera o **maior prazo** entre os livros
  selecionados, acrescida de 2 dias por livro que exceder 2 (regra do
  `EmprestimoService`). Todos os livros ficam indisponíveis.

### CT-10 — Erro ao emprestar para aluno inexistente
- **Pré-condição:** nenhum aluno com o RA "999999" cadastrado.
- **Passos:** informar o RA "999999", selecionar um livro e confirmar.
- **Resultado esperado:** o empréstimo não é criado e a tela exibe o alerta de erro
  "Aluno inexistente". Os livros selecionados continuam disponíveis.

### CT-11 — Erro ao emprestar para aluno com débito
- **Pré-condição:** aluno cadastrado com débito pendente (`possuiDebito = true`).
- **Passos:** informar o RA desse aluno, selecionar um livro e confirmar.
- **Resultado esperado:** o empréstimo não é criado e a tela exibe o alerta de erro
  de débito pendente.

### CT-12 — Erro ao tentar emprestar sem selecionar livro
- **Pré-condição:** tela de empréstimo aberta com livros disponíveis.
- **Passos:** informar o RA e clicar em "Realizar empréstimo" sem marcar nenhum livro.
- **Resultado esperado:** o formulário não é enviado e a tela alerta que é necessário
  selecionar pelo menos um livro (validação em JavaScript).

### CT-13 — Livro indisponível não aparece para empréstimo
- **Pré-condição:** livro já emprestado (indisponível) ou marcado como reservado.
- **Passos:** abrir a tela de empréstimo.
- **Resultado esperado:** o livro não aparece na lista de seleção. Se nenhum livro
  estiver disponível, a tela exibe o aviso "Nenhum livro disponível" e o botão de
  confirmar fica desabilitado.

---

## 4. Devolução de Livros (`/devolucoes/nova`)

### CT-14 — Sucesso na devolução dentro do prazo
- **Pré-condição:** empréstimo ativo cuja data prevista de devolução ainda não passou.
- **Passos:** na tela de devoluções, clicar em "Devolver" no empréstimo desejado.
- **Resultado esperado:** o empréstimo muda para o status DEVOLVIDO com a data efetiva
  registrada, os livros voltam a "Disponível" (reaparecem na tela de empréstimo), a
  mensagem de sucesso é exibida e o empréstimo some da lista de ativos. O aluno
  continua com a situação "Regular".

### CT-15 — Devolução em atraso gera débito para o aluno
- **Pré-condição:** empréstimo ativo com data prevista de devolução já ultrapassada
  (exibido com o indicador "Em atraso" na tela).
- **Passos:** clicar em "Devolver" nesse empréstimo.
- **Resultado esperado:** a devolução é registrada normalmente, porém o aluno passa a
  ter a situação "Com débito" na listagem de alunos e, ao tentar um novo empréstimo
  (CT-11), é bloqueado.

### CT-16 — Erro ao devolver empréstimo já devolvido
- **Pré-condição:** empréstimo com status DEVOLVIDO.
- **Passos:** enviar uma requisição de devolução para esse empréstimo (situação
  possível apenas por requisição direta, já que a tela lista somente os ativos).
- **Resultado esperado:** a operação é rejeitada com a mensagem de que o empréstimo
  já foi devolvido, sem alterar nenhum dado.

### CT-17 — Lista vazia de empréstimos ativos
- **Pré-condição:** nenhum empréstimo com status ATIVO no sistema.
- **Passos:** abrir a tela de devoluções.
- **Resultado esperado:** a tabela exibe a mensagem "Nenhum empréstimo ativo no
  momento" e nenhum botão de devolução é exibido.

