# Filter expressions

Filter expression is a logical expression that can test a method, class
or field (member). When the expression is evaluated against a member
then the result is either `true` or `false`. The Selector class can be
used to compile an expression and then to match against a member, like

    Selector.compile("... expression ... ").match(myMember)

which will be either true or false. The expression can check that the
class, field or method is private, public, abstract, volatile and many
other features. These simple conditions can be used together as a
logical expression containing logical AND, OR and NOT operators,
parentheses and attribute conversions.

For example the filter expression

`public | private`

will be `true` for any field, class or method, whichever the code
queries that is either `private` or `public`, but will
not "select" `protected` or package private members.

You can use `|` to express OR relation and `&` to express AND relation.
For example the filter expression

`public | private & final`

will select the members that are `public` or `private` but the
`private` fields also have to be `final` or else they will not be
selected. (The operator `&` has higher precedence than `|`.)

The expressions can use the `!` character to negate the following
part and `(` and `)` can also be used to override the evaluation
order. 

Sometimes you want to write a condition in part of the expression that
is checking not the actual member but something that is related to that
member. For example you can write the expression

    (simpleName ~ /boolean/ | simpleName ~ /int/) & declaringClass -> !simpleName ~ /Object/

to test methods `equals()` and `hashCode()`. It will check that the
simple name of the return value is `boolean` or `int` and that the
declaring class is not `Object`. The part `declaringClass ->` signals
that the next part of the expression should be evaluated not for the
object but rather for the declaring class of the object. These
conversions are on the same precedence level as the `!` negation
operator and can be used up to any level. For example you can write

    declaringClass -> superClass -> superClass -> !simpleName ~ /Object/

to check that the method is at least three inheritance level deeper
declared than the `Object` class.

The formal BNF definition of the selector expressions is the following:


    EXPRESSION ::= EXPRESSION1 ['|' EXPRESSION1 ]+ 
    EXPRESSION1 ::= EXPRESSION2 ['&amp;' EXPRESSION2] +
    EXPRESSION2 :== TERMINAL | '!' EXPRESSION2 | CONVERSION '->' EXPRESSION2 |'(' EXPRESSION ')' 
    TERMINAL ::= TEST | REGEX_MATCHER
    TEST ::= registered word
    CONVERSION ::= registered conversion
    REGEX_MATCHER ::= registered regex word '~' '/' regular expression '/'

The registered words, regex matchers and conversions are numerous, and
are documented in the following sections. There are many predefined but
the class `Select` provides a possibility to register your own tests,
regex matchers and conversions.

Many of the generators use filter expressions that select certain
members from a class. For example you want to map an object to a
`Map` and you use the `mapper` generator. You want to control which
fields should be stored into the map. This can be done through the
configuration key `filter` specifying a filter expression. The
expression will select certain fields to be included and exclude
others.

Filter expressions are used by many Java::Geci generators and the tool
is a strong weapon in the hands of the generator writers. For example
generators are encouraged to use the configuration parameter `filter` to
filter out methods or fields that need the attendance of the specific
generator. This configuration key is supported out of the box by the
abstract generators `AbstractFilteredMethodsGenerator` and
`AbstractFilteredFieldsGenerator`. Generators extending one of these
abstract generators will get the methods or fields already filtered.
 
<!-- snip documentation snippet="epsilon" 
                       append="snippets='Selector_head_.*'" trim="do"-->

`annotation ~ /regex/` is `true` if the examined member has
an annotation that matches the regular expression.

`annotated` is `true` if the examined member has an
annotation. (Any annotation.)

### Conversion

Conversions are used to direct the next part of the expression to
check something else instead of the member. The conversion is on
the same level as the `!` negation operator and the name of the
conversion is separated from the following part of the expression
by `->`.

* `declaringClass` check the declaring class instead of the
member. This can be applied to methods, fields and classes.
Note that there is an `enclosingClass` that can be applied to
classes.

### Class and method checking selectors

<p> These conditions work on classes and on methods. Applying
them on a field will throw exception.

* `abstract` is `true` if the type of method is abstract.


* `implements` is `true` if the class implements at least one
interface. When applied to a method it is `true` if the
method implements a method of the same name and argument
types in one of the interfaces the class directly or
indirectly implements. In other words it means that there is
an interface that declares this method and this method is an
implementation (not abstract).


### Class checking selectors

These conditions work on classes. When used on a field then
the type of the field is checked. When used on a method then the
return type of the method is checked. When the documentation here
says "... when the type is ..." it means that the class or
interface itself or the type of the field or the return type of
the method in case the condition is checked against a field or
method.

* `interface` is `true` if the type is an interface

* `primitive` is `true` when the type is a primitive type,
a.k.a. `int`, `double`, `char` and so on. Note that `String`
is not a primitive type.

* `annotation` is `true` if the type is an annotation
interface.

* `anonymous` is `true` if the type is anonymous.

* `array` is `true` if the type is an array.

* `enum` is `true` if the type is an enumeration.

* `member` is `true` if the type is a member class, a.k.a.
inner or nested class or interface

* `local` is `true` if the type is a local class. Local
classes are defined inside a method.

* `extends` without any regular expression checks that the
class explicitly extends some other class. (Implicitly
extending `Object` does not count.)


* `extends ~ /regex/` is `true` if the canonical name of the
superclass matches the regular expression. In other words if
the class extends directly the class given in the regular
expression.

* `simpleName ~ /regex/` is `true` if the simple name of the
class (the name without the package) matches the regular
expression.

* `canonicalName ~ /regex/` is `true` if the canonical name of
the class matches the regular expression.

* `implements ~ /regex/` is `true` if the type directly
implements an interface whose name matches the regular
expression. (Note: `implements` can also be used without a
regular expression. In that case the checking is different.)

### Method checking selectors

These conditions work on methods. If applied to anything else
than a method the checking will throw an exception.

* `synthetic` is `true` if the method is synthetic. Synthetic
methods are generated by the Javac compiler in some special
situation. These methods do not appear in the source code.

* `synchronized` is `true` if the method is synchronized.

* `native` is `true` if the method is native.

* `strict` is `true` if the method has the `strict` modifier.
This is a rarely used modifier and affects the floating point
calculation.

* `default` is `true` if the method is defined as a default
method in an interface.

* `bridge` is `true` if the method is a bridge method. Bridge
methods are generated by the Javac compiler in some special
situation. These methods do not appear in the source code.

* `vararg` is `true` if the method is a variable argument
method.

* `overrides` is `true` if the method is overriding another
method in the superclass of the method's declaring method or
a method in the superclass of the superclass and so on.
Implementing a method declared in an interface alone will not
result `true`, even though methods implementing an interface
method are annotated using the compile time `@Override`
annotation. This check is not the same.

* `void` is `true` if the method has no return value.

* `returns ~ /regex/` is `true` if the method return type's
canonical name matches the regular expression.

* `throws ~ /regex/` is `true` if the method throws a declared
exception that matches the regular expression.

* `signature ~ /regex/` checks that the signature of the method
matches the regular expression. The signature of the method
uses the formal argument names `arg0` ,`arg1`,...,`argN`.

### Field checking selectors

These conditions work on fields. If applied to anything else
than a field the checking will throw an exception.

* `transient` is `true` if the field is transient.

* `volatile` is `true` if the field is declared volatile.

### Universal selectors

These conditions work on filds, classes and methods.

* `true` is `true` always.

* `false` is `false` always.

* `null` is `true` when the tested something is null. This can be used to test when a field, class or method
has a parent, enclosing class or something else that we can examine with a `->` operator.

* `private` is `true` if the examined member has private
protection.

* `protected` is `true` if the examined member has protected
protection.

* `package` is `true` if the examined member has package
private protection.

* `public` is `true` if the examined member is public.

* `static` is `true` if the examined member is static.

* `static` is `true` if the examined member is final.

* `name ~ /regex/` is `true` if the examined member's name
matches the regular expression.
<!-- end snip -->
