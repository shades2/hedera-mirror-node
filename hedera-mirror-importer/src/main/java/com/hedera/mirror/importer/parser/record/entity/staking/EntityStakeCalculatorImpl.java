package com.hedera.mirror.importer.parser.record.entity.staking;

/*-
 * ‌
 * Hedera Mirror Node
 * ​
 * Copyright (C) 2019 - 2022 Hedera Hashgraph, LLC
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

import com.google.common.base.Stopwatch;
import java.util.Collection;
import javax.inject.Named;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

import com.hedera.mirror.common.domain.addressbook.NodeStake;
import com.hedera.mirror.importer.parser.record.RecordStreamFileListener;
import com.hedera.mirror.importer.repository.EntityRepository;
import com.hedera.mirror.importer.repository.EntityStakeRepository;

@CustomLog
@Named
@RequiredArgsConstructor
public class EntityStakeCalculatorImpl implements EntityStakeCalculator {

    private final EntityRepository entityRepository;
    private final EntityStakeRepository entityStakeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RecordStreamFileListener recordStreamFileListener;

    @Override
    public void calculate(Collection<NodeStake> nodeStakes) {
        if (nodeStakes.isEmpty()) {
            return;
        }

        var stopwatch = Stopwatch.createStarted();
        // Flush data to final tables so the entity balance is accurate when refreshing the materialized view
        recordStreamFileListener.flush();
        entityRepository.refreshEntityStateStart();
        eventPublisher.publishEvent(new NodeStakeUpdateEvent(this));
        log.info("Flushed data from record file and refreshed entity_state_start materialized view in {} ", stopwatch);
    }

    @Override
    public void update() {
        var stopwatch = Stopwatch.createStarted();
        int count = entityStakeRepository.updateEntityStake();
        log.info("Updated pending reward and stake state for {} entities in {}", count, stopwatch);
    }
}
