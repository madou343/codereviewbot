package org.com.jambit.codereviewbot.summary;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AggregatorTest {

    String promt = "**File:** `pom.xml`  \n" +
            "| Severity | Problem | Source | Affected Component | Concrete Measure (Acceptance Criteria) | Impact/Scope | Links |\n" +
            "|---|---|---|---|---|---|---|\n" +
            "| **Low** | Empty `<license/>`, `<developer/>` and `<scm/>` sections – they add no value and may cause Maven warnings. | `pom.xml` line 15‑31 | Build/Metadata | Remove empty elements or populate with real data; Maven build must finish without “missing information” warnings. | Non‑critical, dev‑only | – |\n" +
            "| **Low** | No `<project.build.sourceEncoding>` defined – default may differ between environments, leading to inconsistent UTF‑8 handling. | `pom.xml` line 1‑30 | Build | Add `<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>` under `<properties>`; verify compiled classes use UTF‑8. | Low (build reproducibility) | – |\n" +
            "| **Low** | No explicit `<maven.compiler.source>` / `<maven.compiler.target>` – relies on parent defaults which may change with future Spring Boot versions. | `pom.xml` line 33‑38 | Build | Add `<maven.compiler.source>${java.version}</maven.compiler.source>` and `<maven.compiler.target>${java.version}</maven.compiler.target>`; CI must confirm compilation with Java 17. | Low (future‑proofing) | – |\n" +
            "| **Low** | No dependency version overrides – currently safe because of Spring Boot parent, but explicit version control improves auditability. | `pom.xml` dependencies block | Dependency Management | Add `<dependencyManagement>` with explicit versions (or comment that parent provides them). | Low (audit) | – |\n" +
            "\n" +
            "---\n" +
            "\n" +
            "**File:** `HomeController.java`  \n" +
            "| Severity | Problem | Source | Affected Component | Concrete Measure (Acceptance Criteria) | Impact/Scope | Links |\n" +
            "|---|---|---|---|---|---|---|\n" +
            "| **High** | **Potential SSRF / open‑network scan** – user‑controlled `url` is fetched without validation or allow‑list, exposing internal services. | `search()` method, line 31‑39 (`new URL(url)`, `InetAddress.getByName`) | Controller / Security | Validate that `url` is an absolute HTTP/HTTPS URL and matches a whitelist (e.g., public domains); reject or sanitize others with HTTP 400. | Prod‑critical, security | OWASP‑SSRF |\n" +
            "| **High** | **No connection timeout** – `HttpURLConnection` may block indefinitely on slow/unreachable hosts, leading to DoS. | `search()` line 44‑48 (`connection.connect()`) | Controller / Performance | Set `connection.setConnectTimeout(5000)` and `connection.setReadTimeout(5000)`; unit test must simulate timeout and verify 504 response. | Prod‑relevant | – |\n" +
            "| **Medium** | **Logging via `System.out/err`** – not thread‑safe and bypasses logging framework, making log aggregation difficult. | `search()` line 23, 57, 68 (`System.out.println`, `System.err.println`) | Logging / Observability | Replace with SLF4J logger (`private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);`) and log at appropriate levels. | Low‑Medium | – |\n" +
            "| **Medium** | **`RestTemplate` instantiated per request** – creates unnecessary objects and disables connection pooling. | `getIpInfo()` line 73 (`new RestTemplate()`) | Performance / Resource Management | Define a `@Bean RestTemplate` (or use `WebClient`) and inject it; verify bean reuse in integration test. | Low‑Medium | – |\n" +
            "| **Medium** | **Missing input validation for `ip` parameter** – malformed IP may cause external request errors or injection attacks. | `getIpInfo()` line 78 (`restTemplate.getForObject`) | Security / Reliability | Validate `ip` against IPv4/IPv6 regex before request; return HTTP 400 on invalid format. | Low‑Medium | – |\n" +
            "| **Low** | **Returning raw `Server` header** – may expose internal server details to callers. | `search()` line 55 (`serverInfo = connection.getHeaderField(\"Server\")`) | Security / Information Leakage | Omit `Server` header from response or mask it; add test asserting field is not present. | Low | – |\n" +
            "| **Low** | **No exception handling for non‑200 responses from ipinfo.io** – any 4xx/5xx bubbles up as generic 500, losing context. | `getIpInfo()` catch‑all `Exception` block | Resilience | Catch `HttpClientErrorException`/`HttpServerErrorException`, forward appropriate status code and body; add test for 404 case. | Low | – |\n" +
            "| **Low** | **Method returns `ResponseEntity<?>` with raw `Map.of`** – type safety is lost and serialization may change. | `search()` return statement | API Stability | Define a DTO class `WebsiteStatusDto` with explicit fields; map values to it; ensure JSON contract is stable. | Low | – |\n" +
            "| **Low** | **No Javadoc / method comments** – reduces maintainability. | Entire class | Documentation | Add Javadoc describing purpose, parameters, possible errors, and security considerations. | Low | – |\n" +
            "\n" +
            "---\n" +
            "\n" +
            "**File:** `MainApplication.java`  \n" +
            "| Severity | Problem | Source | Affected Component | Concrete Measure (Acceptance Criteria) |\n" +
            "|---|---|---|---|---|\n" +
            "| **Low** | No class‑level Javadoc describing the application entry point. | `MainApplication` class | Documentation | Add Javadoc explaining bootstrapping and any required environment variables. |\n" +
            "\n" +
            "---\n" +
            "\n" +
            "**File:** `DemoApplicationTests.java`  \n" +
            "| Severity | Problem | Source | Affected Component | Concrete Measure (Acceptance Criteria) |\n" +
            "|---|---|---|---|---|\n" +
            "| **Low** | Test only verifies context loads; no functional tests for `HomeController`. | `DemoApplicationTests` class | Test Coverage | Add integration tests (e.g., `@WebMvcTest`) covering `/search` and `/searchip` with mocked services; CI must report ≥80 % controller coverage. |\n" +
            "\n" +
            "---\n" +
            "\n" +
            "### Gesamturteil  \n" +
            "**⭐\uFE0F⭐\uFE0F⭐\uFE0F⭐\uFE0F☆ (4 von 5 Sternen)**  \n" +
            "Das Projekt ist funktional, aber kritische Sicherheitslücken (SSRF, fehlende Timeouts) und fehlende Validierung gefährden den Produktivbetrieb. Code‑Qualität, Logging und Testabdeckung können deutlich verbessert werden.\n" +
            "\n" +
            "### Top‑3 To‑Dos\n" +
            "1. **Implementiere strenge URL‑ und IP‑Validierung + Whitelisting** – eliminiert SSRF‑Risiko.  \n" +
            "2. **Setze Connection‑ und Read‑Timeouts** für alle Netzwerkaufrufe und verwende einen gemeinsam konfigurierten `RestTemplate`/`WebClient`.  \n" +
            "3. **Ersetze System‑Print‑Aufrufe durch SLF4J‑Logging** und führe umfassende Integrationstests für beide Endpunkte ein.";

    @Test
    void aggregatorTest() {

        List<String> inputListe = Arrays.asList(promt);

        Aggregator aggregator = new Aggregator();
        String output = aggregator.aggregate(inputListe);
        System.out.println(output);

    }

}