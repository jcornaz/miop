rootProject.name = "miop"

include("miop-common")
include("miop-jvm")
include("miop-js")

include("miop-javafx")

include("miop-collekt-common")
include("miop-collekt-js")
include("miop-collekt-jvm")

include("miop-internal-test-common")
include("miop-internal-test-jvm")
include("miop-internal-test-js")

project(":miop-common").projectDir = file("core/common")
project(":miop-jvm").projectDir = file("core/jvm")
project(":miop-js").projectDir = file("core/js")

project(":miop-javafx").projectDir = file("integration/javafx")

project(":miop-collekt-common").projectDir = file("integration/collekt/common")
project(":miop-collekt-js").projectDir = file("integration/collekt/js")
project(":miop-collekt-jvm").projectDir = file("integration/collekt/jvm")

project(":miop-internal-test-common").projectDir = file("internal/test/common")
project(":miop-internal-test-jvm").projectDir = file("internal/test/jvm")
project(":miop-internal-test-js").projectDir = file("internal/test/js")
