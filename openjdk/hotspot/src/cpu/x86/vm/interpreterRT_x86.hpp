/*
 * Copyright 1998-2005 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

// native method calls

class SignatureHandlerGenerator: public NativeSignatureIterator {
 private:
  MacroAssembler* _masm;
#ifdef AMD64
#ifdef _WIN64
  unsigned int _num_args;
#else
  unsigned int _num_fp_args;
  unsigned int _num_int_args;
#endif // _WIN64
  int _stack_offset;
#else
  void move(int from_offset, int to_offset);
  void box(int from_offset, int to_offset);
#endif // AMD64

  void pass_int();
  void pass_long();
  void pass_float();
#ifdef AMD64
  void pass_double();
#endif // AMD64
  void pass_object();

 public:
  // Creation
  SignatureHandlerGenerator(methodHandle method, CodeBuffer* buffer) : NativeSignatureIterator(method) {
    _masm = new MacroAssembler(buffer);
#ifdef AMD64
#ifdef _WIN64
    _num_args = (method->is_static() ? 1 : 0);
    _stack_offset = (Argument::n_int_register_parameters_c+1)* wordSize; // don't overwrite return address
#else
    _num_int_args = (method->is_static() ? 1 : 0);
    _num_fp_args = 0;
    _stack_offset = wordSize; // don't overwrite return address
#endif // _WIN64
#endif // AMD64
  }

  // Code generation
  void generate(uint64_t fingerprint);

  // Code generation support
  static Register from();
  static Register to();
  static Register temp();
};
