package de.fosd.typechef.crefactor.backend.refactor

import de.fosd.typechef.parser.c.{FunctionDef, Id}
import de.fosd.typechef.crefactor.backend.Connector
import de.fosd.typechef.conditional.{Opt, One}
import de.fosd.typechef.typesystem.CUnknown
import de.fosd.typechef.crewrite.{ConditionalNavigation, ASTNavigation, ASTEnv}

/**
 * Helper object providing some useful functions for refactorings.
 */
object Helper extends ASTNavigation with ConditionalNavigation {

  val languageKeywords = List(
    "auto",
    "break",
    "case",
    "char",
    "const",
    "continue",
    "default",
    "do",
    "double",
    "else",
    "enum",
    "extern",
    "float",
    "for",
    "goto",
    "if",
    "inline",
    "int",
    "long",
    "register",
    "restrict",
    "return",
    "short",
    "signed",
    "sizeof",
    "static",
    "struct",
    "switch",
    "typedef",
    "union",
    "unsigned",
    "void",
    "volatile",
    "while",
    "_Alignas",
    "_Alignof",
    "_Atomic",
    "_Bool",
    "_Complex",
    "_Generic",
    "_Imaginary",
    "_Noreturn",
    "_Static_assert",
    "_Thread_local"
  )

  /**
   * Checks if the name of a variable is compatible to the iso c standard. See 6.4.2 of the iso standard
   *
   * @param name name to check
   * @return <code>true</code> if valid, <code>false</code> if not
   */
  def isValidName(name: String): Boolean = {
    if (!name.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
      return false
    }
    if (name.startsWith("__")) {
      // reserved
      return false
    }
    !isReservedLanguageKeyword(name)
  }

  /**
   * Checks if the name is a language keyword.
   *
   * @param name the name to check
   * @return <code>true</code> if language keyword
   */
  def isReservedLanguageKeyword(name: String): Boolean = {
    languageKeywords.contains(name)
  }

  def isStructOrUnion(id: Id): Boolean = {
    try {
      val env = Connector.getEnv(id)
      env.structEnv.someDefinition(id.name, true) || env.structEnv.someDefinition(id.name, false)
    } catch {
      case e: Exception => return false
    }
  }

  def isTypedef(id: Id): Boolean = {
    try {
      val env = Connector.getEnv(id)
      env.typedefEnv(id.name) match {
        case One(CUnknown(_)) => return false
        case _ => return true
      }
    } catch {
      case e: Exception => return false
    }
  }

  def getFunctionDefOpt(entry: Product, astENV: ASTEnv): Opt[_] = {
    val parent = parentOpt(entry, astENV)
    parent match {
      case o: Opt[_] => {
        o.entry match {
          case FunctionDef(_, _, _, _) => o
          case _ => getFunctionDefOpt(parent, astENV)
        }
      }
      case _ => null
    }
  }
}