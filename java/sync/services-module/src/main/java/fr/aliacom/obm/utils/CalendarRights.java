/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.utils;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.obm.sync.Right;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

public final class CalendarRights implements Iterable<CalendarRightsPair> {

   public static class Builder {
       private ImmutableMap.Builder<String, Set<Right>> calendarToRightsBuilder;

       private Builder() {
           calendarToRightsBuilder = ImmutableMap.builder();
       }

       public Builder fromMap(Map<String, EnumSet<Right>> calendarToRights) {
           calendarToRightsBuilder.putAll(calendarToRights);
           return this;
       }

       public Builder addRights(String calendar, EnumSet<Right> rights) {
           calendarToRightsBuilder.put(calendar, Sets.immutableEnumSet(rights));
           return this;
       }

       public CalendarRights build() {
           return new CalendarRights(calendarToRightsBuilder.build());
       }
   }

   private static class PairIterator implements Iterator<CalendarRightsPair> {
       private Iterator<Map.Entry<String, Set<Right>>> iterator;

       private PairIterator(Iterator<Map.Entry<String, Set<Right>>> iterator) {
           this.iterator = iterator;
       }

       @Override
       public boolean hasNext() {
           return iterator.hasNext();
       }

       @Override
       public CalendarRightsPair next() {
           Map.Entry<String, Set<Right>> entry = iterator.next();
           return new CalendarRightsPair(entry.getKey(), entry.getValue());
       }

       @Override
       public void remove() {
           iterator.remove();
       }

   }

   public static Builder builder() {
       return new Builder();
   }

   private final Map<String, Set<Right>> calendarToRights;

   private CalendarRights(Map<String, Set<Right>> calendarToRights) {
       this.calendarToRights = calendarToRights;
   }

   public Optional<Set<Right>> getRights(String calendar) {
       return Optional.fromNullable(this.calendarToRights.get(calendar));
   }

   @Override
   public Iterator<CalendarRightsPair> iterator() {
       return new PairIterator(this.calendarToRights.entrySet().iterator());
   }

   @Override
   public boolean equals(Object other) {
       return other instanceof CalendarRights ?
               this.calendarToRights.equals(((CalendarRights) other).calendarToRights) :
               false;
   }

   @Override
   public int hashCode() {
       return Objects.hashCode(this.calendarToRights);
   }
}
