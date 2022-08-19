/* Copyright (C) 2021 – University of Mons, University Antwerpen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.ac.umons.jsonlearning.oracles;

import be.ac.umons.jsonvalidation.JSONSymbol;
import be.ac.umons.jsonvalidation.WordConversion;
import de.learnlib.api.oracle.SingleQueryOracle.SingleQueryCounterValueOracle;
import net.automatalib.words.Word;

/**
 * Counter value oracle for JSON documents.
 * 
 * It counts the number of unmatched {@code {} and {@code [}.
 * 
 * @author Gaëtan Staquet
 */
public class JSONCounterValueOracle implements SingleQueryCounterValueOracle<JSONSymbol> {

    @Override
    public Integer answerQuery(Word<JSONSymbol> prefix, Word<JSONSymbol> suffix) {
        Word<JSONSymbol> word = prefix.concat(suffix);
        return Utils.countUnmatched(WordConversion.fromJSONSymbolWordToString(word));
    }
    
}
