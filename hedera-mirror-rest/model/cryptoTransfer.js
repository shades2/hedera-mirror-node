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

'use strict';

const BaseModel = require('./baseModel');

class CryptoTransfer extends BaseModel {
  /**
   * Parses crypto_transfer table columns into object
   */
  constructor(cryptoTransfer) {
    super();
    this.amount = cryptoTransfer.amount;
    this.consensusTimestamp = cryptoTransfer.consensus_timestamp;
    this.entityId = cryptoTransfer.entity_id;
  }

  static tableAlias = 'ctr';
  static tableName = 'crypto_transfer';

  static AMOUNT = 'amount';
  static CONSENSUS_TIMESTAMP = 'consensus_timestamp';
  static ENTITY_ID = 'entity_id';
}

module.exports = CryptoTransfer;
