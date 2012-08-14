The CoreASM Modeling Framework
==============================

CoreASM is an open source project that focuses on offering a lean executable
ASM (Abstract State Machines) language, in combination with a supporting tool
environment for high-level design, experimental validation and formal
verification (where appropriate) of abstract system models.

CoreASM offers a rich ASM language with a formally defined semantics that is
faithful to the original semantics of ASM and is the first ASM tool that
explicitly supports distributed ASM computation models with custom scheduling
policies. The extensibility of its language and modeling environment, the most
significant feature of CoreASM, provides utmost flexibility for extending its
language definition and execution engine in order to tailor it to the
particular needs of virtually any conceivable application context. CoreASM is
one of the few ASM tools that is implemented as an open framework. 

CoreASM can be used in differnt forms: it offers a modeling framework, it can
be viewed as a tool suite with different tools it offers around the engine,
while the engine itself can be used as an stand-alone tool (using its
command-line or Eclipse plugin UI).

### What is Carma?

Carma is a command-line CoreASM Engine driver. It runs CoreASM specifications
using a CoreASM Engine and offers control over the engine.  


### How is it related to CoreASM on SourceForge?

These are the same projects. At some point in 2012 we have stopped maintaing the
code on sourceforge and started moving the repository to github.


How To Build CoreASM?
---------------------

You can build the CoreASM core projects using [Maven](http://maven.apache.org). 
Just run the following command in `org.coreasm.parent` project:

    mvn clean install

To build/update eclipse project files, run:

    mvn eclipse:eclipse


The Latest Version
------------------

Latest released versions of Carma and the CoreASM Engine can be found on the CoreASM 
Project web site <http://www.coreasm.org>. 


Licensing
---------
 
Both the Carma project and the CoreASM Engine project are licensed under the 
Academic Free License version 3.0 which is available from either of these
web pages: 
  <http://www.opensource.org/licenses/afl-3.0.php>
  <http://www.coreasm.org/afl-3.0.php>


Thanks for using Carma.

The CoreASM Development Team
2005-2012

