# Documentum Generation using SNIPPETS

Software is as good as it is used. Good and up-to-date documentation
is needed for the use of software and to be honest: good documentation is
rare. There are many aspects to create good documentation like good
grammar, easy to understand sentences, up-to-date facts, examples in the
documentation and many others. Docugen code generator working inside the
Java::Geci framework can help to keep the documentation up-to-date
reducing redundancy.

Documentation is redundant many times. This is not a problem.
Documentation is not code. When we read the documentation the
information has to be consistent and has to be there where we actually
are in the reading and understanding process. Many times it means that
the same text appears at different places. It gets there using copy and
paste. Similarly program documentation many times contains code
examples. Sometimes these examples are written by the document
writer/editor (bad practice), other times, they are written by the
programmers as example programs and the editors only copy the code to the
documentation.

Documentation has to follow the change of the code. If the code changes
the relevant, but only the relevant parts of the documentation has to be
updates.

If the copy and paste or update operations are performed each time the
original text or the code changes then the documentation is up-to-date.
Usually, these operations, when done by a human are not performed,
performed late or with mistakes. The reason for this is that these are
boring and mechanical tasks which humans are not good at. The same time
computers can be asked to perform these tasks and this is what
Java::Geci snippet handling does.

## Using snippets

To have Java::Geci do these copy-paste operations the parts of the text,
or program code that needs to appear elsewhere as well has to be
signaled. This is done putting a special line containing

```
snippet snippet_name parameters
```

where the text starts and putting a special line containing

```
end snippet
```

after the last line of the text. These two lines define a snippet. The
words `snippet` and `end snippet` are configurable in the code generator
that collects the snippets. By default, these are the strings and the
word `snippet` can also be written as `snipet` if you like to misspell
the word `snippet`.

Snippets are essentially text lines. Each snippet has a name and
optionally has parameters. The snippet collectors usually collect the
lines from the sources recognizing the start and the end lines. For
example, the class `SnippetCollector` collects the snippets with the
start and end lines as described above. If only requires that the line
contains* the work `snippet` there is a name after it and optionally
parameters. Since it requires that it only *contains* these parts the
Java comment sequence `//`, `/*` or `*/` can precede or follow the
snippet starting or ending characters.


## Snippet handling generators

Snippet handling is implemented by many different generators. Thes are
not conventional generators in the sense that many of them  do not
generate code. They, however, implement the `Generator` interface and
thus work inside the Java::geci framework and they can be configured the
same way as many other generators.

There are three types of snippet handling generators:

1. **Snippet collectors** that read the source files that the Java::Geci
interface lists. The snippets are collected into a `SnippetStore`
object, which itself is stored in the context managed by the framework.
Since this context is shared between the different generators the
`SippetStore` object is available to all snippet handling generators
that run controlled by the same Geci object or by Geci objects that
they share the same context. The `SnippetStore` can store a
snippet and can also retrieve a snippet using its name.

1. **Snippet modifiers** that modify the already collected snippets.
There are snippet modifying generators that trim off the spaces on the
left of the lines, perform search and replace on the lines using regular
expression, delete certain lines from snippets, join different snippets
together, number the lines and so on. These snippet modifiers are
controlled by the parameters of the source segments that use the actual
snippet(s) and to let different segments to use the same snippet with
individual modifications these snippet modifying generators create
copies of the snippets they modify.
 
1. **Snippet inserters** that insert the optionally modified snippets
into the text where they are needed in different segments. These are
real generators in the sense that they really modify the code, even if
the code is documentation. They do it the standard way Java::Geci
support writing segments and possibly failing the unit test when the
documentation *was* not up-to-date.

Later in this document, we will write about the available snippet
handling generators and how to use them.

## Snippet Strategy

Before getting into the details of the different snippet handling
generators let's talk about the strategy on how to use snippets.

When the document contains sample code then it is fairly
straightforward what a snippet is. It is the code of the unit test that
plays the role of the documentation. The code is inserted into the
documentation as formatted code. 

There parts of the document that are repeated. It is also possible to
have hyperlinks and references to the text, but many times it is better
to repeat the text. In this case, one part of the document can play the
role of the snippet and the other parts may be the segments where the
snippet will be copied. This approach needs a lot of discipline no to
mix up the original snippet text and the copies. Many times it is better
to store these snippets in one or many snippet files, which is not part
of the documentation and the documentation files all contain copies.

Another use of the snippets when part of the documentation has its
natural place in the code itself. A good example is the configuration
parameters of the generators. Generators usually have an inner class
named `Config` with many fields. When a new configuration parameter is
inserted or deleted or the use of it changes then this is more likely to
update the documentation if it is attached directly to the field as a
comment/snippet than if it is in a separate documentation file.
Although there is a high chance that the comment, which is part of the
documentation will also be outdated as comments usually are outdated.
Even though, the chances are a slim better than in case of a separate
documentation.

## Generator Phases

Code generators usually work alone without the aid of other generators.
Snippet handling generators are different. They cooperate heavily. In
this case, it has the consequence that there has to be a specific order of
how these generators are executed. It is obvious that we cannot trim the
lines of the snippets eliminating the tabbing on the left side of the
lines if they were not collected yet. Also trimming is not possible when
the lines were already numbered. In this case, the order is fairly
obvious.

In other cases, the ordering may not be so simple and obvious. We may
need to delete certain lines from a code sample and want to number the
lines so that the text can reference the individual code lines in the
explanation. In this case, numbering comes after the deletion of
certain lines. A different code sample, however, needs to number the
lines first and then delete the lines that we do not want in the
documentation. That way the numbering follows the numbering of the
original lines in the code sample and it clearly shows in the
documentation that some of the lines were deleted.

The ordering of the execution of the generators is generally up to the
framework, but generators can decide in which phase they want to run.
The framework executes the generators in multiple phases many times and
it consults each generator in each phase whether it needs execution that
phase or not.

Snippet handling generators need execution only in a single phase and
they can be configured during their creation (using the builder) which
phase they should run. If the line trimming should run before numbering
then the generator `SnippetTrim` should have a smaller phase serial
number than `SnippetNumberer`. The snippet handling generator build up usually looks like
the one that is used to create this documentation:

<!-- snip TestGenerateJavageciDocumentation trim="to=0"-->
```java
final var geci = new Geci();
int i = 0;
Assertions.assertFalse(
    geci
        .source("..", ".").ignore("\\.git", "\\.(png|zip|class|jar|asc|graffle)$", "target")
        .log(Geci.MODIFIED)
        .register(SnippetCollector.builder().phase(i++).build())
        .register(SnippetAppender.builder().phase(i++).build())
        .register(SnippetRegex.builder().phase(i++).build())
        .register(SnippetTrim.builder().phase(i++).build())
        .register(SnippetNumberer.builder().phase(i++).build())
        .register(SnipetLineSkipper.builder().phase(i++).build())
        .register(MarkdownCodeInserter.builder().phase(i++).build())
        .splitHelper("md", new MarkdownSegmentSplitHelper())
        .generate(),
    geci.failed());
```

The generators are registered in the order they are to be executed and
the call to `phase(i++)` registers the phases 0, 1, 2 ... and so on.

## Implemented Snippet Handling Generators

As there are three types of snippet handling generators we will discuss
the implemented generators in three subsections.

### Collector

Currently, there is only one snippet collecting generator (unless this
document is out of date). The generator `SnippetCollector`. It is
executed by the framework for all sources that are configured and
collected and the collector scans through the lines of the sources and
collects the snippets.

It starts to collect lines after a line that matches the regular
expression named `snippetStart` and before `snippetEnd`. These regular
expressions have default values and as they are listed in the `Config`
inner class of the generator `SnippetCollector`:

<!-- snip SnippetCollector_config trim="to=0" regex="replace='/snippetEnd/snippetEnd  /' replace='/private Pattern//' replace='/~);//' replace='/Pattern.compile~(//' escape='~'"-->
```java
snippetStart = "(?://|/\\*)\\s*snipp?et\\s+(.*)$"
snippetEnd   = "(?://\\s*end\\s+snipp?et|end\\s+snipp?et\\s*\\*/)"
```

The characters that follow the `snippet` to the end of the line are
parameters. Syntactically they are the same as the parameters in the
`@Geci` annotations or in the editor-fold segments. The first word is
the name/id of the snippet the rest are parameters. The snippet is
stored using the name and this name is used when later the snippet is
referenced in the text that needs the snippet to be inserted. The
parameters are stored along with the snippet and are available for
snippet modifying and snippet inserting generators.

### Modifiers

Snippet modifiers run when the snippets are collected and modify the
lines of the snippets. Because the same snippet may be used with
different modifications for different segments modifiers create a copy
of the snippet before any modification is performed. The copy is
assigned to the segment where the snippet will be inserted.

#### Snippet Store and Snippet Copies

Keeping track of the copies is automatically done by the snippet store.
The snippet store stores the original snippets as they are collected and
also, copies that are identified by the name of the snippet and by the
id of the segment.

For example, the snippet named `SnippetCollector_config` is used a few
lines above and the use is configured to perform certain modifications.
Namely, these are trimming, and replacing declaration clutter (types,
modifiers and so on) that is important in the Java code but distracts
in the documentation. We can insert the same snippet without any
modifications again:

<!-- snip SnippetCollector_config_other snippet="SnippetCollector_config"-->
```java
        private Pattern snippetStart = Pattern.compile("(?://|/\\*)\\s*snipp?et\\s+(.*)$");
        private Pattern snippetEnd = Pattern.compile("(?://\\s*end\\s+snipp?et|end\\s+snipp?et\\s*\\*/)");
```

#### Segments for Snippets

The snippets are inserted into segments like any other generated code.
These segment, however, are usually not editor-fold segments. Editor
fold segments are useful for Java code, but are not useful for markdown
or other documentation format. When the framework Java::Geci is searching
the segments in source codes it uses segment split helpers. These helper
classes can give hint to the framework in the process splitting up a
source into segments. The one that supports splitting up Java source
code is automatically configured. Segments that are for other formatted
files have to be registered. If we look again at the code that is used
to create this documentation:
                                                          
<!-- snip splitHelperRegistering snippet="TestGenerateJavageciDocumentation" trim="to=0" number="do"-->
```java
1. final var geci = new Geci();
2. int i = 0;
3. Assertions.assertFalse(
4.     geci
5.         .source("..", ".").ignore("\\.git", "\\.(png|zip|class|jar|asc|graffle)$", "target")
6.         .log(Geci.MODIFIED)
7.         .register(SnippetCollector.builder().phase(i++).build())
8.         .register(SnippetAppender.builder().phase(i++).build())
9.         .register(SnippetRegex.builder().phase(i++).build())
10.         .register(SnippetTrim.builder().phase(i++).build())
11.         .register(SnippetNumberer.builder().phase(i++).build())
12.         .register(SnipetLineSkipper.builder().phase(i++).build())
13.         .register(MarkdownCodeInserter.builder().phase(i++).build())
14.         .splitHelper("md", new MarkdownSegmentSplitHelper())
15.         .generate(),
16.     geci.failed());
```

we can have a look at line 14. It registers the
`MarkdownSegmentSplitHelper` class and associates it with the file
extension `md`. (We could also write `.md`.) For files that have the
extension `.md` this segment split helper will be used. For everything
else, the default Java segment split helper.

The way segment split helpers work is that they can tell where a segment
starts and where it ends. Most of the helpers (as a matter of fact both
of them that have been developed so far) are based on regular expression
matching. The `MarkdownSegmentSplitHelper` defines the following regular
expression patterns:

<!-- snip MarkdownSegmentSplitHelper_patterns skip="do" trim="do" regex="replace='/e_n/en/'"-->
```java
                //startPattern
                "^(\\s*)\\[//]:\\s*#\\s*\\(\\s*snip\\s+(.*)\\)\\s*$" +
                    "|" +
                        "^(\\s*)<!--\\s*snip\\s+(.*)-->\\s*$"
                // endPattern
                "^(\\s*)```(\\s*)$" +
                    "|" +
                        "^(\\s*)\\[//]:\\s*#\\s*\\(\\s*end\\s+snip\\s*(.*)\\)\\s*$" +
                    "|" +
                        "^(\\s*)<!--\\s*end\\s+snip\\s*(.*)-->\\s*$"
```

This shows that in the case of markdown the segments can start with an HTML
comment that contains the word `snip` at the start and then it is
followed by the segment parameters or it can be a special

```
 []: # (snip ...) 
 ```

line which is a weird hack in markdown and will not get into the HTML
output and generally can be used as a comment.

The end of a segment is either three backticks or an HTML comment or a
markdown comment as the example shows above that contain the words `end
snip`. It is important to note that the `MarkdownCodeInserter` generator
that inserts the code into the segments look at the first line of the
segment that is already there and in case it starts with three backtick
then it keeps that line and modifies only the rest of the lines writing
the content of the snippet there.

The segment that starts with `snip` and is followed by the parameters
should have an id. This id is either specified as the first word on the
line after the word `snip` or it is the value of the parameter `id`. The
snippet that is to be used in this segment is specified using the
parameter `snippet`. In case there is no parameter named `snippet` then
the id of the segment will be used. The segments that are used to
accommodate snippets have to be uniquely named. Although it is
absolutely fine to have segments with the same name in different source
files when we generate code into Java programs the snippet handling may
get confused. If two different segments share the same name even in
different files and use the same snippet then they will share the same
copy of the snippet. All the modifications done by one of the uses will
also affect the use of the snippet in the other segment. As a rule of
thumb: segments using snippets have to be uniquely named. If more than
one segment uses the same snippet then only one can use the name of the
snippet as segment id, the others should have different identifiers and
refer to the snippet using the `snippet` parameter.

#### Invocation of Modifiers

A snippet modifying generator runs only if it is registered in the Geci
execution for the appropriate phase and only for the segments that
configures the execution of that modifier.

Each snippet modifier generators has a mnemonic. The use of this
mnemonic is a bit different than in case Java code generators, but on
a high level, it is very similar. It is a word that identifies the
generator.

When the segment has a parameter, which is the mnemonic of the generator
then the generator will be executed for that segment, and it will
execute the modifications it has to perform on the segment copy of the
snippet.

The format of the parameter with the mnemonic is the same as the format
of the parameter lines of the segment. The first word can be the id of
the configuration (this is optional and usually not used) and the rest
is `key=value` pairs. Note that these `key=value` pairs are already
inside a string thus the `"` character has to be quoted in case `"` was
used to enclose the parameter itself. For example the snippet insertion
above in the markdown code looks like the following:

```
snip MarkdownSegmentSplitHelper_patterns skip="do" trim="do" regex="replace='/e_n/en/'"
```

The identifier of the segment is `MarkdownSegmentSplitHelper_patterns`
and this is also the name of the snippet it uses. The snippet when
collected contains some lines that we do not need in the documentation,
therefore, the generator with the mnemonic `skip` is used. Also, the lines
have to be trimmed therefore `trim` is also configured. As a matter of
fact, the skipper generator does not need many configurations and the
trimming generator is invoked using the default parameters. In this case,
we just write "do" as a configuration string. In that case, the id of the
configuration is `do` and this is generally ignored.

It is important that in case a snippet modifier is to be executed but
does not need configuration then it cannot just be configured with the
empty string. For example `trim=""` will not run the trimmer. The empty
string means that the modifier is not configured to run.

The generator with the mnemonic `regex` is configured with one
parameter. This parameter is `replace` and it will instruct the
generator to replace every occurrence of `e_n` to `en`. (It is only
there for demonstration purposes only, there is no `e_n` in the actual
snippet.)

#### Configuration for all Snippet Modifiers

The following configuration options are available for all the snippet
modifiers. These configuration options are defined in the abstract class
`AbstractSnippeter`, which is extended by every snippet modifier class
and thus they inherit these configuration options. The other
configuration options that are specific to an individual snippet
modifier are detailed separately for each.

<!-- snip AbstractSnippeter_configuration snippet="epsilon" trim="to=0" append="snippets='AbstractSnippeter_config_.*'" regex="replace='/protected~s~w+~s/##### `/' replace='/;.*$/`/' replace='|^~s*/~*||' escape='~'" skip="do"-->
##### `phase = 1`


The phase parameter defines the phase that the snippet
modifying generator is to be run. As this is not a `String`
parameter it can only be configured in the builder when the
generator instance is created. The generator will return the
value `phase + 1` when the framework queries the number of
phases the generator needs and when asked if it has to be
active in a phase it will return `true` if the actual phase is
the same as the one configured.

##### `files = "\\.md$"`


This configuration parameter can limit the file name pattern
for which the snippet generator will run. The default value is
to run for every file that has the extension `.md`. If you have
other file extensions in your documentation you can configure it
in the builder interface.

<!-- end snip -->

#### Snippet Modifiers

In this section, we document the individual snippet modifiers one by one.

##### Trim

Trim is implemented in the generator class `SnippetTrim`. It can remove
the spaces from the left of the lines keeping the original tabbing.
When a snippet is collected from some deeply nested code structure then
it usually happens that each line starts with many spaces. This would
push the code in the documentation to the right leaving a huge gutter on
the left. This modifier removes the extra spaces.

The configuration parameters are the followings:

<!-- snip SnippetTrim_config snippet="epsilon" trim="to=0" append="snippets='SnippetTrim_config_.*'" regex="replace='/private~s~w+~s/##### `/' replace='/;.*$/`/' replace='|^~s*/~*||' escape='~'" skip="do"-->
##### `to = "0"`


This parameter can define the number of spaces on the left of
the lines. Although the parameter is a string the value should
obviously, be an integer number as it is recommended to specify
it without `"` or `'` characters surrounding, just simply, for
example

               trim="to=2"


<!-- end snip -->

##### Regex

<!-- snip SnippetRegex_doc regex="replace='/^~s*~*~s?//' escape='~'"-->

The `regex` snippet generator goes through each line of the snippet
and does regular expression based search and replace. It can be used
for example as

     regex="replace='/a/b/'"

to replace each occurrence of `a` to `b`.

A real life example is:

     regex="replace='/^\\\\s*\\\\*\\\\s?//'

It is used in the documentation of docugen to include the snippet
defined in the JavaDoc of the class `SnippetRegex` and to remove the
`*` characters from the start of the line. The result is what you are
reading now (or you may be reading the original JavaDoc in the Java
file.)

The replace configuration string should have the syntax

    W + search string + W + replace string + W

where `W` is just any character. It is usually `/` so that the search
and replace string looks like the usual vi editor search and replace.

When we write regular expressions inside the `replace` string it is
interpreted as a Java string already twice. This means that the
escape characters used in regular expressions as well as in strings,
the backslash characters had to be repeated four times. This would
greatly decrease readability. Instead of `\s` we could write
`\\\\s`. (As a matter of fact it is a possibility.)

To lessen the number of backslash characters and to avoid building
`\\\\\\` fences instead of coding it is possible to define a
character that is used instead of `\` in the regular expressions. The
configuration parameter is `escape` and you can write on a segment
line

    regex="replace='/^~s*~*~s?//' escape='~'

to use the tilde character instead of

    regex="replace='/^\\\\s*\\\\*\\\\s?//'

The recommendation is to use the tilde characters. There can only be
one `escape` definition in a single segment, but there can be many
`replace` strings and they will be executed in the order they are
defined.

Note that the default escape string is a string and not only a single
character, but it does not really make sense to use many characters
when the major aim of this configuration is to shorten the regular
expression escape sequences.

If you want to use the same escape string in all the snippet regex
modifications then you can configure it in the builder when the
regex object is created.

<!-- end snip -->

##### Number

<!-- snip SnippetNumberer_doc regex="replace='/^~s*~*~s?//' escape='~'"-->

The `number` snippet handling generator is implemented in the class `SnippetNumberer`.
It can number the individual lines in a snippet with increasing numbers. The result can be used
to have some code with line numbers so that the surrounding text can reference the actual lines.

<!-- end snip -->


The configuration parameters for the numberer:

<!-- snip SnippetNumberer_config snippet="epsilon" trim="to=0" append="snippets='SnippetNumberer_Config_.*'" regex="replace='/private~s~w+~s/##### `/' replace='/;.*$/`/' replace='|^~s*/~*||' escape='~'" skip="do"-->
##### `start = "1"`


This is the number that will be used to denote the first line. The default is to start from one and to use
increasing numbers.

##### `step = "1"`


This configuration parameter controls the increment between the consecutive numbers. If you want to number
the lines in step of ten, like we did old times in BASIC then you can set `number="step='10'"`.

##### `format = "%d. "`


This configuration option controls how the numbers are formatted. This is the string that is passed to the
`String.format()` method as first argument when converting the line number to string.

##### `from = "0"`


When not all the lines are needed to be numbered in a snippet you can limit the numbering to start at
certain line and finish at another line. This parameter can specify the index of the first line that
is to be numbered. The default value is zero, which means that the very first line already will be numbered.
If you want to number starting with the second line then you can use `number="from=1"` as a configuration.
If you want to number only the last three lines then you can configure the generator on the segment
prividing the parameters `number="from=-3"`.

##### `to = ""`


You can limit the end of the numbering. This parameter defines the index of the last line that is numbered
exclusive. If you want to number only the first three lines you can configure the generator as
`number="to=3"`. That way only the lines with the indexes `0`, `1` and `2` will be numbered.

Negative numbers, just as in case of `from` count backward from the end of the snippet. Empty value, which is
the default means no limit.

<!-- end snip -->

##### Append
<!-- snip SnippetAppender_doc regex="replace='/^~s*~*~s?//' escape='~'"-->

Snippet appender can be used to append multiple snippets together. This is useful when the content of the
documentation is in different places and it would be inconvinient to include the individual snippets one by one
into the documentation. The snippets can be referenced using their name and the reference can also contain regular
expression. In that case all the snippets that match the regular expression will be joined together and added to the
actual snippet in alphabetical order.

The generator has one configuration parameter `snippets` (note the plural) that should list the snippets that are to
be appended to the original snippet. Remember that the base snippet is referenced in the segment using the parameter
`snippet` or using the name of the segment id as a snippet name by default and the appended snippets are appended to
this snippet.

There can be multiple `snippets` values on the configuration of the snippet appender. They are worked up from left to
right and when a value matches many snippet names as a regular expression then these snippets will be appended in
alphabetical order. The usual practice is to name these snippets something like `snippetName_001`, `snippetName_002`
and so on and use the configuration `append="snippets='snippetName_.*'"`.

The regular expressions sometimes may need escaping and the same way as in case of the snippet modifying generator
`regex` there is a parameter `escape7 that can be used to specify the character that is used instead of `\\\\`.

Sometimes there is no base snippet to append to. The documentation, for example, needs to join together the snippets
`manySniipets_.*` that are `manySniipets_001`, `manySniipets_002`, and `manySniipets_003`. In that case if the
segment references the first snippet, `snippet="manySniipets_001" append="snippets='manySniipets_.*'"` then the first
snippet will be copied twice. For this reason there is a predefined snippet in the snippet store called `epsilon`
that contains no lines. Using that we can write

snippet="epsilon" append="snippets='manySniipets_.*'"

The snippets are copied from the original, unmodified version of the snippet. It is not possible to execute
modifications on the different snippets and perform the appending afterwards.

<!-- end snip -->

<!-- snip SnippetAppender_config snippet="epsilon" trim="to=0" append="snippets='SnippetAppender_Config_.*'" regex="replace='/private~s(?:~w+|List<String>)~s/##### `/' replace='/;.*$/`/' replace='|^~s*/~*||' escape='~'" skip="do"-->
##### `snippets = List.of()`


This configuration parameter defines the snippets that are appended to the base snippet.

##### `escape = ""`


This parameter can be used to define an escape character / string that can be used instead of the backslash
character that would be needed four times to be used in the regular expression. It is recommended to use the
tilde `~` character.

<!-- end snip -->


##### Skip

<!-- snip SnipetLineSkipper_doc regex="replace='/^~s*~*~s?//' escape='~'"-->
<!-- end snip -->

### Snippet Inserters

In the case of markdown formatted code is
enclosed between three starting and three ending back-ticks on a line.

When handling markdown Java::Geci needs the `MarkdownSegmentSplitHelper`
registered with the appropriate file extension. 

<!-- snip TestGenerateJavageciDocumentation trim="to=0"-->
```java
final var geci = new Geci();
int i = 0;
Assertions.assertFalse(
    geci
        .source("..", ".").ignore("\\.git", "\\.(png|zip|class|jar|asc|graffle)$", "target")
        .log(Geci.MODIFIED)
        .register(SnippetCollector.builder().phase(i++).build())
        .register(SnippetAppender.builder().phase(i++).build())
        .register(SnippetRegex.builder().phase(i++).build())
        .register(SnippetTrim.builder().phase(i++).build())
        .register(SnippetNumberer.builder().phase(i++).build())
        .register(SnipetLineSkipper.builder().phase(i++).build())
        .register(MarkdownCodeInserter.builder().phase(i++).build())
        .splitHelper("md", new MarkdownSegmentSplitHelper())
        .generate(),
    geci.failed());
```