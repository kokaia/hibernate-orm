/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.entity;

import java.util.function.Consumer;

import org.hibernate.Incubating;
import org.hibernate.mapping.IndexedConsumer;
import org.hibernate.metamodel.mapping.AttributeMapping;

/**
 * This is essentially a List of AttributeMapping(s), but exposing
 * an interface which is more suitable to our needs; in particular
 * it expresses the immutable nature of this structure, and allows
 * us to extend it with additional convenience methods such as
 * {@link #indexedForEach(IndexedConsumer)}.
 * And additional reason for the custom interface is to allow
 * custom implementations which can be highly optimised as
 * necessary for our specific needs; for example the
 * implementation {@link org.hibernate.persister.internal.ImmutableAttributeMappingList}
 * is able to avoid caching problems related to JDK-8180450, which would
 * not have been possible with a standard generic container.
 * @since 6.2
 */
@Incubating
public interface AttributeMappingsList extends Iterable<AttributeMapping> {

	int size();

	AttributeMapping get(int i);

	void forEach(Consumer<? super AttributeMapping> attributeMappingConsumer);

	void indexedForEach(IndexedConsumer<? super AttributeMapping> consumer);

}