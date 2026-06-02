# Deploy no Render

Configuração recomendada para este projeto:

- Environment: Docker
- Root Directory: `dungeons-dragons-api`
- Dockerfile Path: `Dockerfile`
- Branch: `main`

Após subir os arquivos no GitHub, execute no Render:

`Manual Deploy > Clear build cache & deploy`

O Dockerfile usa Java 21, compatível com o `pom.xml`.

## Rodando localmente

Dentro da pasta `dungeons-dragons-api`, execute:

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Acesse:

- Front local: http://localhost:8080/index.html
- Swagger: http://localhost:8080/swagger-ui/index.html
