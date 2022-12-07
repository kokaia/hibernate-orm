package org.hibernate.orm.test.timezones;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.SybaseDialect;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.Setting;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DomainModel(annotatedClasses = PassThruZonedTest.Zoned.class)
@SessionFactory
@ServiceRegistry(settings = @Setting(name = AvailableSettings.TIMEZONE_DEFAULT_STORAGE, value = "NORMALIZE"))
public class PassThruZonedTest {

	@Test void test(SessionFactoryScope scope) {
		ZonedDateTime nowZoned = ZonedDateTime.now().withZoneSameInstant( ZoneId.of("CET") );
		OffsetDateTime nowOffset = OffsetDateTime.now().withOffsetSameInstant( ZoneOffset.ofHours(3) );
		long id = scope.fromTransaction( s-> {
			Zoned z = new Zoned();
			z.zonedDateTime = nowZoned;
			z.offsetDateTime = nowOffset;
			s.persist(z);
			return z.id;
		});
		scope.inSession( s-> {
			Zoned z = s.find(Zoned.class, id);
			ZoneId systemZone = ZoneId.systemDefault();
			ZoneOffset systemOffset = systemZone.getRules().getOffset( Instant.now() );
			if ( scope.getSessionFactory().getJdbcServices().getDialect() instanceof SybaseDialect) {
				// Sybase with jTDS driver has 1/300th sec precision
				assertEquals( nowZoned.toInstant().truncatedTo(ChronoUnit.SECONDS), z.zonedDateTime.toInstant().truncatedTo(ChronoUnit.SECONDS));
				assertEquals( nowOffset.toInstant().truncatedTo(ChronoUnit.SECONDS), z.offsetDateTime.toInstant().truncatedTo(ChronoUnit.SECONDS));
			}
			else {
				assertEquals( nowZoned.withZoneSameInstant(systemZone), z.zonedDateTime );
				assertEquals( nowOffset.withOffsetSameInstant(systemOffset), z.offsetDateTime );
			}
			assertEquals( systemZone, z.zonedDateTime.getZone() );
			assertEquals( systemOffset, z.offsetDateTime.getOffset() );
		});
	}

	@Entity
	public static class Zoned {
		@Id
		@GeneratedValue Long id;
		ZonedDateTime zonedDateTime;
		OffsetDateTime offsetDateTime;
	}
}