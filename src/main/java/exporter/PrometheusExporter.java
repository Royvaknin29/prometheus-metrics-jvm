package exporter;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnClass(CollectorRegistry.class)
public class PrometheusExporter {
    private static final CollectorRegistry metricRegistry = CollectorRegistry.defaultRegistry;

    @Bean
    public void PrometheusExporter() {
        List<Collector> collectors = new ArrayList<>();
        collectors.add(new StandardExports());
        collectors.add(new MemoryPoolsExports());
        collectors.add(new GarbageCollectorExports());
        collectors.add(new ThreadExports());
        collectors.add(new BufferPoolsExports());
        collectors.add(new ClassLoadingExports());

        for (Collector collector : collectors) {
            collector.register();
        }
    }

    @Bean
    ServletRegistrationBean registerPrometheusExporterServlet() {
        return new ServletRegistrationBean(new MetricsServlet(metricRegistry), "/metrics");
    }
}
