package com.hedera.mirror.importer.parser.record.transactionhandler;

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

import java.util.ArrayList;
import java.util.Collection;
import javax.inject.Named;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;

import com.hedera.mirror.common.domain.addressbook.NetworkStake;
import com.hedera.mirror.common.domain.addressbook.NodeStake;
import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hedera.mirror.common.domain.transaction.Transaction;
import com.hedera.mirror.common.domain.transaction.TransactionType;
import com.hedera.mirror.common.util.DomainUtils;
import com.hedera.mirror.importer.parser.record.entity.EntityListener;
import com.hedera.mirror.importer.parser.record.entity.staking.EntityStakeCalculator;
import com.hedera.mirror.importer.repository.NodeStakeRepository;
import com.hedera.mirror.importer.util.Utility;

@CustomLog
@Named
@RequiredArgsConstructor
class NodeStakeUpdateTransactionHandler implements TransactionHandler {
    private final EntityListener entityListener;
    private final EntityStakeCalculator entityStakeCalculator;
    private final NodeStakeRepository nodeStakeRepository;

    @Override
    public EntityId getEntity(RecordItem recordItem) {
        return null;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.NODESTAKEUPDATE;
    }

    @Override
    public void updateTransaction(Transaction transaction, RecordItem recordItem) {
        long consensusTimestamp = recordItem.getConsensusTimestamp();
        if (!recordItem.isSuccessful()) {
            var record = recordItem.getRecord();
            var status = record.getReceipt().getStatus();
            log.warn("NodeStakeUpdateTransaction at {} failed with status {}", consensusTimestamp, status);
            return;
        }

        // We subtract one since we get stake update in current day, but it applies to previous day
        long epochDay = Utility.getEpochDay(consensusTimestamp) - 1L;
        var transactionBody = recordItem.getTransactionBody().getNodeStakeUpdate();
        long stakingPeriod = DomainUtils.timestampInNanosMax(transactionBody.getEndOfStakingPeriod());
        long stakeTotal = transactionBody.getNodeStakeList().stream().map(n -> n.getStake()).reduce(0L, Long::sum);

        NetworkStake networkStake = new NetworkStake();
        networkStake.setConsensusTimestamp(consensusTimestamp);
        networkStake.setEpochDay(epochDay);
        networkStake.setMaxStakingRewardRatePerHbar(transactionBody.getMaxStakingRewardRatePerHbar());
        networkStake.setNodeRewardFeeDenominator(transactionBody.getNodeRewardFeeFraction().getDenominator());
        networkStake.setNodeRewardFeeNumerator(transactionBody.getNodeRewardFeeFraction().getNumerator());
        networkStake.setStakeTotal(stakeTotal);
        networkStake.setStakingPeriod(stakingPeriod);
        networkStake.setStakingPeriodDuration(transactionBody.getStakingPeriod());
        networkStake.setStakingPeriodsStored(transactionBody.getStakingPeriodsStored());
        networkStake.setStakingRewardFeeDenominator(transactionBody.getStakingRewardFeeFraction().getDenominator());
        networkStake.setStakingRewardFeeNumerator(transactionBody.getStakingRewardFeeFraction().getNumerator());
        networkStake.setStakingRewardRate(transactionBody.getStakingRewardRate());
        networkStake.setStakingStartThreshold(transactionBody.getStakingStartThreshold());
        entityListener.onNetworkStake(networkStake);

        var nodeStakesProtos = transactionBody.getNodeStakeList();
        if (nodeStakesProtos.isEmpty()) {
            return;
        }

        nodeStakeRepository.evictNodeStakeCache();
        Collection<NodeStake> nodeStakes = new ArrayList<>();
        for (var nodeStakeProto : nodeStakesProtos) {
            NodeStake nodeStake = new NodeStake();
            nodeStake.setConsensusTimestamp(consensusTimestamp);
            nodeStake.setEpochDay(epochDay);
            nodeStake.setMaxStake(nodeStakeProto.getMaxStake());
            nodeStake.setMinStake(nodeStakeProto.getMinStake());
            nodeStake.setNodeId(nodeStakeProto.getNodeId());
            nodeStake.setRewardRate(nodeStakeProto.getRewardRate());
            nodeStake.setStake(nodeStakeProto.getStake());
            nodeStake.setStakeNotRewarded(nodeStakeProto.getStakeNotRewarded());
            nodeStake.setStakeRewarded(nodeStakeProto.getStakeRewarded());
            nodeStake.setStakingPeriod(stakingPeriod);
            nodeStakes.add(nodeStake);
            entityListener.onNodeStake(nodeStake);
        }

        entityStakeCalculator.calculate(nodeStakes);
    }
}
