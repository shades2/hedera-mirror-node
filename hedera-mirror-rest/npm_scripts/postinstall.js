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

import {execSync} from 'child_process';
import path from 'path';

if (
  process.env.NODE_ENV === 'production' ||
  process.env.npm_config_only === 'prod' ||
  process.env.npm_config_only === 'production'
) {
  process.exit(0);
}

// run "husky install" only when dev dependencies are installed
const huskyPath = ['hedera-mirror-rest', '.husky'].join(path.sep);
console.log(execSync(`cd .. && husky install ${huskyPath}`, {encoding: 'utf8'}));
