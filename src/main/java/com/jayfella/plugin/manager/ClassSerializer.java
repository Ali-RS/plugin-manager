package com.jayfella.plugin.manager;

import com.jayfella.plugin.manager.plugin.description.PluginDescription;
import org.objectweb.asm.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassSerializer {

    private static final Logger log = Logger.getLogger(ClassSerializer.class.getName());

    ClassSerializer() {

    }

    public byte[] processClass(PluginDescription pdf, String path, byte[] clazz) {
        try {
            clazz = convert(clazz);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Fatal error trying to convert " + pdf.getFullName() + ":" + path, ex);
        }

        return clazz;
    }

    private byte[] convert(byte[] b)
    {
        ClassReader cr = new ClassReader( b );
        ClassWriter cw = new ClassWriter( cr, 0 );

        cr.accept( new ClassVisitor( Opcodes.ASM7, cw )
        {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
            {
                return new MethodVisitor( api, super.visitMethod( access, name, desc, signature, exceptions ) )
                {

                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String desc)
                    {
                        super.visitFieldInsn( opcode, owner, name, desc );
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf)
                    {
                        super.visitMethodInsn( opcode, owner, name, desc, itf );
                    }
                };
            }
        }, 0 );

        return cw.toByteArray();
    }

}
