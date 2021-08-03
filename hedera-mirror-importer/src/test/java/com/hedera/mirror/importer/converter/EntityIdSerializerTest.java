package com.hedera.mirror.importer.converter;

/*-
 * ‌
 * Hedera Mirror Node
 * ​
 * Copyright (C) 2019 - 2021 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hedera.mirror.importer.domain.EntityId;
import com.hedera.mirror.importer.domain.EntityTypeEnum;

@ExtendWith(MockitoExtension.class)
class EntityIdSerializerTest {
    @Mock
    JsonGenerator jsonGenerator;

    @Test
    void testNull() throws Exception {
        // when
        new EntityIdSerializer().serialize(null, jsonGenerator, null);

        // then
        verify(jsonGenerator).writeNull();
    }

    @Test
    void testEmpty() throws Exception {
        // when
        new EntityIdSerializer().serialize(EntityId.EMPTY, jsonGenerator, null);

        // then
        verify(jsonGenerator).writeNumber(EntityId.EMPTY.getId());
    }

    @Test
    void testEntity() throws Exception {
        // when
        var entity = EntityId.of(10L, 20L, 30L, EntityTypeEnum.ACCOUNT);
        new EntityIdSerializer().serialize(entity, jsonGenerator, null);

        // then
        verify(jsonGenerator).writeNumber(entity.getId());
    }
}
