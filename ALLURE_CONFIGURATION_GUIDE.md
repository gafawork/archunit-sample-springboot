# Configuração do Allure Framework para JUnit 5

Guia passo a passo da configuração do [Allure Report](https://allurereport.org/) neste projeto, usado para gerar relatórios de execução de testes JUnit 5 (incluindo os testes de arquitetura do ArchUnit, como o `DDDArchitectureMetricsTest`).

## 1. Propriedades de versão (`pom.xml`)

```xml
<properties>
    <java.version>21</java.version>
    <archunit.version>1.4.2</archunit.version>
    <allure.version>2.35.3</allure.version>
    <aspectj.version>1.9.25</aspectj.version>
</properties>
```

## 2. Importar o BOM do Allure

O BOM garante que todos os artefatos do Allure usados no projeto fiquem na mesma versão.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.qameta.allure</groupId>
            <artifactId>allure-bom</artifactId>
            <version>${allure.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 3. Dependência de integração com JUnit 5

```xml
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

> `allure-jupiter` é o novo nome do artefato `allure-junit5` (renomeado a partir da versão 2.35.0 do Allure Java). As coordenadas antigas continuam resolvendo via *relocation* do Maven, mas o nome atual é o recomendado para projetos novos.

## 4. Weaving do AspectJ (necessário para `@Step` e `@Attachment`)

As anotações `@Step` e `@Attachment` do Allure dependem de weaving via AspectJ em tempo de execução. Isso é feito adicionando um `-javaagent` ao `maven-surefire-plugin`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>
            -javaagent:"${settings.localRepository}/org/aspectj/aspectjweaver/${aspectj.version}/aspectjweaver-${aspectj.version}.jar"
        </argLine>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>${aspectj.version}</version>
        </dependency>
    </dependencies>
</plugin>
```

## 5. Plugin de geração de relatório

O plugin `allure-maven` permite gerar/abrir o relatório sem precisar instalar o Allure CLI manualmente:

```xml
<plugin>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-maven</artifactId>
    <version>2.15.2</version>
    <configuration>
        <reportVersion>${allure.version}</reportVersion>
    </configuration>
</plugin>
```

## 6. Diretório de resultados (`allure.properties`)

Arquivo: `src/test/resources/allure.properties`

```properties
allure.results.directory=target/allure-results
```

## 7. Anotando os testes

Exemplo aplicado em `src/test/java/com/example/user/architecture/DDDArchitectureMetricsTest.java`:

- `@Epic("Arquitetura")` e `@Feature("Métricas DDD")` na classe, para agrupar o teste no relatório.
- `@Story("...")` no método de teste, descrevendo o cenário.
- `@Step("...")` em métodos privados auxiliares, para que cada etapa (importação de classes, cálculo de cada grupo de métricas) apareça detalhada no relatório.
- `Allure.addAttachment(...)` para anexar o relatório textual das métricas como um arquivo `.txt` no resultado do teste, em vez de apenas imprimir no console.

```java
@Epic("Arquitetura")
@Feature("Métricas DDD")
public class DDDArchitectureMetricsTest {

    @Test
    @Story("Cálculo de métricas de acoplamento e visibilidade do domínio")
    void calculateDddArchitectureMetrics() {
        // ...
        Allure.addAttachment("Relatório de métricas DDD", "text/plain", report.toString(), ".txt");
    }

    @Step("Importar classes do pacote {basePackage}")
    private JavaClasses importClasses(String basePackage) {
        return new ClassFileImporter().importPackages(basePackage);
    }

    // demais métodos anotados com @Step ...
}
```

## 8. Rodando os testes e gerando o relatório

```bash
# Executa os testes e grava os resultados em target/allure-results
./mvnw test

# Baixa o Allure CLI (se necessário) e abre o relatório no navegador
./mvnw allure:serve

# Ou apenas gera o relatório HTML estático em target/site/allure-maven-plugin
./mvnw allure:report
```

## Verificação

Após rodar `./mvnw test`, o diretório `target/allure-results` deve conter arquivos como:

- `*-result.json` — resultado do teste, com tags `epic`/`feature`/`story` e os steps registrados.
- `*-container.json` — metadados de execução (before/after, etc.).
- `*-attachment.txt` — o relatório de métricas anexado via `Allure.addAttachment`.
