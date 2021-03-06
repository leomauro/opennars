/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fge.grappa.transform.generate;

import com.github.fge.grappa.run.context.Context;
import com.github.fge.grappa.transform.base.InstructionGraphNode;
import com.github.fge.grappa.transform.base.InstructionGroup;
import com.github.fge.grappa.transform.base.ParserClassNode;
import com.github.fge.grappa.transform.base.RuleMethod;
import com.github.fge.grappa.transform.process.GroupClassGenerator;
import com.github.fge.grappa.transform.runtime.BaseAction;
import me.qmx.jitescript.util.CodegenUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.IRETURN;

// TODO: move to process/ subpackage
public final class ActionClassGenerator
    extends GroupClassGenerator
{
    public ActionClassGenerator(final boolean forceCodeBuilding)
    {
        super(forceCodeBuilding);
    }

    @Override
    public boolean appliesTo(@Nonnull final ParserClassNode classNode,
        @Nonnull final RuleMethod method)
    {
        Objects.requireNonNull(method, "method");
        return method.containsExplicitActions();
    }

    @Override
    protected boolean appliesTo(final InstructionGraphNode group)
    {
        return group.isActionRoot();
    }

    @Override
    protected Type getBaseType()
    {
        return Type.getType(BaseAction.class);
    }

    @Override
    protected void generateMethod(final InstructionGroup group,
        final ClassWriter cw)
    {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "run",
            CodegenUtils.sig(boolean.class, Context.class), null, null);

        insertSetContextCalls(group, 1);
        convertXLoads(group);

        group.getInstructions().accept(mv);

        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0); // trigger automatic computing
    }
}
