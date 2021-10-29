/* Copyright (C) 2013-2021 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
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
package be.ac.umons.vpdajson.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.abstractimpl.AbstractVPDAlphabet;
import net.automatalib.words.impl.GrowingMapAlphabet;

/**
 * A {@link VPDAlphabet} implementation that allows to add new symbols after its construction.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class GrowingVPDAlphabet<I> extends AbstractVPDAlphabet<I> {

    private final List<I> allSyms;
    private final GrowingAlphabet<I> callSyms;
    private final GrowingAlphabet<I> internalSyms;
    private final GrowingAlphabet<I> returnSyms;
    private final Map<I, SymbolType> typeOfSymbol;

    public GrowingVPDAlphabet() {
        this(new GrowingMapAlphabet<>(), new GrowingMapAlphabet<>(), new GrowingMapAlphabet<>());
    }

    private GrowingVPDAlphabet(GrowingAlphabet<I> internalSyms, GrowingAlphabet<I> callSyms, GrowingAlphabet<I> returnSyms) {
        super(internalSyms, callSyms, returnSyms);
        this.internalSyms = internalSyms;
        this.callSyms = callSyms;
        this.returnSyms = returnSyms;
        this.allSyms = new ArrayList<>();
        this.typeOfSymbol = new HashMap<>();
    }

    public boolean addNewSymbol(final I symbol, final SymbolType type) {
        if (containsSymbol(symbol)) {
            return false;
        }

        final GrowingAlphabet<I> localList;
        switch (type) {
            case CALL:
                localList = callSyms;
                break;
            case RETURN:
                localList = returnSyms;
                break;
            default:
                localList = internalSyms;
                break;
        }

        allSyms.add(symbol);
        localList.add(symbol);
        typeOfSymbol.put(symbol, type);
        
        return true;
    }

    @Override
    public SymbolType getSymbolType(I symbol) {
        return typeOfSymbol.get(symbol);
    }

    @Override
    public int size() {
        return allSyms.size();
    }

    @Override
    public I getSymbol(int index) {
        return allSyms.get(index);
    }

    @Override
    public int getSymbolIndex(I symbol) {
        if (!containsSymbol(symbol)) {
            throw new IllegalArgumentException();
        }

        return allSyms.indexOf(symbol);
    }

    @Override
    public boolean containsSymbol(I symbol) {
        return allSyms.indexOf(symbol) != -1;
    }

}
