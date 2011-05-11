# NetLogo QTJ extension

This package contains the NetLogo QTJ (QuickTime for Java) extension.

## Using

The QTJ extension is pre-installed in NetLogo. For instructions on using QuickTime for Java, or for more information about NetLogo extensions, see the NetLogo User Manual.

## Building

Before building, you may need to get QTJava.zip from your QuickTime installation, copy it here, and rename it QTJava.jar. (The Makefile will attempt first to copy it from a known location.)

Use the NETLOGO environment variable to tell the Makefile which NetLogoLite.jar to compile against.  For example:

    NETLOGO=/Applications/NetLogo\\\ 5.0 make

If compilation succeeds, `qtj.jar` will be created.

The Makefile redirects the error output of javac to /dev/null to avoid printing spurious warnings.  Even if we call javac -nowarn it still prints:

    Note: Some input files use or override a deprecated API.
    Note: Recompile with -Xlint:deprecation for details.

and http://bugs.sun.com/view_bug.do?bug_id=6460147 makes it impossible to make it completely go away.  We on the NetLogo team don't want to see these lines every time we build extensions, especially since we can't see which extension is producing it (since they build in parallel), so we suppress  them by redirecting.  Sorry, it's a kludge. If you are actually doing development work on the extension, you may want to remove the redirection so you can see any additional errors and warnings that javac produces.

## Terms of Use

All contents © 2009-2011 Uri Wilensky.

The contents of this package may be freely copied, distributed, altered, or otherwise used by anyone for any legal purpose.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
