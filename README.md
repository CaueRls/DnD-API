# 🐉 Dungeons & Dragons API 

Uma API REST completa e robusta desenvolvida em Java e Spring Boot para gerenciamento de elementos do universo de Dungeons & Dragons (5ª Edição). O projeto conta com paginação, validações avançadas de dados e navegabilidade via HATEOAS.

---

## 🛠️ Tecnologias Utilizadas

* **Java 21** (LTS)
* **Spring Boot 3.3.0**
* **Spring Data JPA** (Persistência de dados)
* **H2 Database** (Banco de dados em memória para ambiente de desenvolvimento)
* **Spring HATEOAS** (Navegabilidade de endpoints)
* **Springdoc OpenAPI (Swagger UI)** (Documentação interativa)
* **Jakarta Validation** (Regras de validação de negócios)

---

## 🗺️ Arquitetura e Entidades (Modelos)

O sistema foi modelado para cobrir três tipos fundamentais de relacionamentos em bancos de dados relacionais:

1. **`Spell` (Magia):** Contém nome, nível, descrição e a escola mágica (`MagicSchool` Enum).
2. **`CharacterClass` (Classe):** Classes de personagens (ex: Mago, Guerreiro) com o dado de vida correspondente (`HitDie` Enum).
   * 🔗 *Relacionamento Many-to-Many:* Uma Classe pode ter várias Magias, e uma Magia pode pertencer a várias Classes.
3. **`Subclass` (Subclasse):** Especializações de uma classe pai (ex: Escola de Evocação para o Mago).
   * 🔗 *Relacionamento One-to-Many:* Uma Classe possui várias Subclasses.
4. **`Monster` (Monstro):** Entidade para criaturas e desafios (ex: Beholder, Dragão).
5. **`MonsterStats` (Atributos):** Pontuações de atributos (Força, Destreza, etc.) limitadas entre 1 e 30.
   * 🔗 *Relacionamento One-to-One:* Cada monstro possui exatamente uma ficha de atributos exclusiva.

---

## 🚀 Como Rodar o Projeto

### Pré-requisitos
* JDK 21 instalado configurado nas variáveis de ambiente.
* Maven instalado (ou utilizar o wrapper `./mvnw`).

### Passo a Passo
1. Clone o repositório para sua máquina local:
   ```bash
   git clone [https://github.com/SEU_USUARIO/NOME_DO_REPOSITORIO.git](https://github.com/SEU_USUARIO/NOME_DO_REPOSITORIO.git)
