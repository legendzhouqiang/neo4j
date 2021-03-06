/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.index.schema.fusion;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import org.neo4j.internal.helpers.collection.Iterables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.neo4j.kernel.impl.index.schema.fusion.IndexSlot.GENERIC;
import static org.neo4j.kernel.impl.index.schema.fusion.IndexSlot.LUCENE;

class InstanceSelectorTest
{
    @Test
    void shouldSelect()
    {
        // given
        InstanceSelector<String> selector = selector(
                LUCENE, "0",
                GENERIC, "1" );

        // when
        String select0 = selector.select( LUCENE );
        // then
        assertEquals( "0", select0 );

        // when
        String select1 = selector.select( GENERIC );
        // then
        assertEquals( "1", select1 );
    }

    @Test
    void shouldThrowOnNonInstantiatedSelect()
    {
        // given
        InstanceSelector<String> selector = selector( LUCENE, "0" );

        assertThrows( IllegalStateException.class, () -> selector.select( GENERIC ) );
    }

    @Test
    void shouldThrowOnNonInstantiatedFlatMap()
    {
        // given
        InstanceSelector<String> selector = selector( LUCENE, "0" );

        // when
        assertThrows( IllegalStateException.class, () -> selector.transform( Integer::parseInt ) );
    }

    @Test
    void shouldThrowOnNonInstantiatedMap()
    {
        // given
        InstanceSelector<String> selector = selector( LUCENE, "0" );

        // when
        assertThrows( IllegalStateException.class, () -> selector.map( Integer::parseInt ) );
    }

    @Test
    void shouldFlatMap()
    {
        // given
        InstanceSelector<String> selector = selectorFilledWithOrdinal();

        // when
        List<Integer> actual = Iterables.asList( selector.transform( Integer::parseInt ) );
        List<Integer> expected = Arrays.stream( IndexSlot.values() ).map( Enum::ordinal ).collect( Collectors.toList() );

        // then
        assertEquals( expected.size(), actual.size() );
        for ( Integer i : expected )
        {
            assertTrue( actual.contains( i ) );
        }
    }

    @Test
    void shouldMap()
    {
        // given
        InstanceSelector<String> selector = selectorFilledWithOrdinal();

        // when
        EnumMap<IndexSlot,Integer> actual = selector.map( Integer::parseInt );

        // then
        for ( IndexSlot slot : IndexSlot.values() )
        {
            assertEquals( slot.ordinal(), actual.get( slot ).intValue() );
        }
    }

    @SuppressWarnings( "ResultOfMethodCallIgnored" )
    @Test
    void shouldThrowOnNonInstantiatedForAll()
    {
        // given
        InstanceSelector<String> selector = selector( LUCENE, "0" );

        // when
        assertThrows( IllegalStateException.class, () -> selector.forAll( Integer::parseInt ) );
    }

    @Test
    void shouldForAll()
    {
        // given
        InstanceSelector<String> selector = selectorFilledWithOrdinal();

        // when
        MutableInt count = new MutableInt();
        selector.forAll( s -> count.increment() );

        // then
        assertEquals( IndexSlot.values().length, count.intValue() );
    }

    @SuppressWarnings( "ResultOfMethodCallIgnored" )
    @Test
    void shouldNotThrowOnNonInstantiatedClose()
    {
        // given
        InstanceSelector<String> selector = selector( LUCENE, "0" );

        // when
        selector.close( Integer::parseInt );

        // then
        // good
    }

    @Test
    void shouldCloseAll()
    {
        // given
        InstanceSelector<String> selector = selectorFilledWithOrdinal();

        // when
        MutableInt count = new MutableInt();
        selector.close( s -> count.increment() );

        // then
        assertEquals( IndexSlot.values().length, count.intValue() );
    }

    private static InstanceSelector<String> selector( Object... mapping )
    {
        EnumMap<IndexSlot,String> map = new EnumMap<>( IndexSlot.class );
        int i = 0;
        while ( i < mapping.length )
        {
            map.put( (IndexSlot) mapping[i++], (String) mapping[i++] );
        }
        return new InstanceSelector<>( map );
    }

    private static InstanceSelector<String> selectorFilledWithOrdinal()
    {
        EnumMap<IndexSlot,String> map = new EnumMap<>( IndexSlot.class );
        for ( IndexSlot slot : IndexSlot.values() )
        {
            map.put( slot, Integer.toString( slot.ordinal() ) );
        }
        return new InstanceSelector<>( map );
    }
}
