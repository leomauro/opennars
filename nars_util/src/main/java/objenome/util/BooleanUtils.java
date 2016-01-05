/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX: genetic programming software for research
 * 
 * EpochX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EpochX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
 * 
 * The latest version is available from: http://www.epochx.org
 */
package objenome.util;

/**
 * This class provides static utility methods for working with booleans and
 * boolean arrays.
 */
public enum BooleanUtils {
    ;

    /**
     * Translates a <code>String</code> of zero ('0') and one ('1') characters
     * to an array of <code>boolean</code>. <code>0</code> characters are
     * translated to a <code>false</code> value, and <code>1</code> characters
     * to <code>true</code> value.
     *
     * @param input a <code>String</code> made up of '1' and '0' characters.
     * @return an array of <code>boolean</code> values which is equivalent to
     * the input <code>String</code>.
     */
    public static boolean[] toArray(CharSequence input) {
        int len = input.length();
        boolean[] bools = new boolean[len];

        for (int i = 0; i < len; i++) {
            bools[i] = (input.charAt(i) == '1');
        }

        return bools;
    }

    /**
     * Provides an alternative to
     * <code>BooleanUtils.generateBoolSequences(int)</code> particularly for
     * larger numbers of bits greater than 30 which that method will struggle
     * with. The order of the booleans generated by this method are identical to
     * that method, so that calling it with an index parameter of n will return
     * a <code>boolean[]</code> identical to the nth element of the
     * <code>boolean[][]</code> returned by
     * <code>BooleanUtils.generateBoolSequences(int)</code>.
     *
     * @param noBits the number of <code>boolean</code> values in which
     * different combinations are made.
     * @param index an index into the possible combinations to allow iteration
     * through the possibilities. There will be 2<sup>noBits</sup>
     * combinations and so indexes up to 2<sup>noBits-1</sup> will be valid.
     * @return an array of booleans. The value of the booleans is identical to
     * the nth element of the result from
     * <code>BooleanUtils.generateBoolSequences</code>, where
     * <code>n == index</code>. Each valid index value will return a unique
     * combination of booleans.
     */
    public static boolean[] generateBoolSequence(int noBits, long index) {
        boolean[] inputs = new boolean[noBits];

        for (int i = 0; i < noBits; i++) {
            int rep = (int) Math.pow(2, i + 1);

            inputs[i] = (index % rep) > Math.floor(rep / 2.0f) - 1;
        }

        return inputs;
    }
}
