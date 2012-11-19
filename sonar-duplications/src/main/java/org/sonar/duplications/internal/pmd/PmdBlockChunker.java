/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2012 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.duplications.internal.pmd;

import com.google.common.collect.Lists;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.ByteArray;

import java.util.Collections;
import java.util.List;

/**
 * Differences with {@link org.sonar.duplications.block.BlockChunker}:
 * works with {@link TokensLine},
 * sets {@link Block#getStartUnit() startUnit} and {@link Block#getEndUnit() endUnit} - indexes of first and last token for this block.
 */
public class PmdBlockChunker {

  private static final long PRIME_BASE = 31;

  private final int blockSize;
  private final long power;

  public PmdBlockChunker(int blockSize) {
    this.blockSize = blockSize;

    long pow = 1;
    for (int i = 0; i < blockSize - 1; i++) {
      pow = pow * PRIME_BASE;
    }
    this.power = pow;
  }

  public List<Block> chunk(String resourceId, List<TokensLine> fragments) {
    List<TokensLine> filtered = Lists.newArrayList();
    int i = 0;
    while (i < fragments.size()) {
      TokensLine first = fragments.get(i);
      int j = i + 1;
      while (j < fragments.size() && fragments.get(j).getValue().equals(first.getValue())) {
        j++;
      }
      if (i < j - 1) {
        TokensLine last = fragments.get(j - 1);
        filtered.add(new TokensLine(first.getStartUnit(), last.getEndUnit(), first.getStartLine(), last.getEndLine(), first.getValue()));
      } else {
        filtered.add(fragments.get(i));
      }
      i = j;
    }
    fragments = filtered;

    if (fragments.size() < blockSize) {
      return Collections.emptyList();
    }
    TokensLine[] fragmentsArr = fragments.toArray(new TokensLine[fragments.size()]);
    List<Block> blocks = Lists.newArrayListWithCapacity(fragmentsArr.length - blockSize + 1);
    long hash = 0;
    int first = 0;
    int last = 0;
    for (; last < blockSize - 1; last++) {
      hash = hash * PRIME_BASE + fragmentsArr[last].getHashCode();
    }
    Block.Builder blockBuilder = Block.builder().setResourceId(resourceId);
    for (; last < fragmentsArr.length; last++, first++) {
      TokensLine firstFragment = fragmentsArr[first];
      TokensLine lastFragment = fragmentsArr[last];
      // add last statement to hash
      hash = hash * PRIME_BASE + lastFragment.getHashCode();
      // create block
      Block block = blockBuilder
          .setBlockHash(new ByteArray(hash))
          .setIndexInFile(first)
          .setLines(firstFragment.getStartLine(), lastFragment.getEndLine())
          .setUnit(firstFragment.getStartUnit(), lastFragment.getEndUnit())
          .build();
      blocks.add(block);
      // remove first statement from hash
      hash -= power * firstFragment.getHashCode();
    }
    return blocks;
  }

}
