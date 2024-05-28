BUILD_DIR = ./build
RTL_DIR = $(BUILD_DIR)/rtl

TOP = TL2AXI4

FPGATOP = bridge.Main

TOP_V = $(RTL_DIR)/$(TOP).v

SCALA_FILE = $(shell find ./src/main/scala -name '*.scala')

TIMELOG = $(BUILD_DIR)/time.log
TIME_CMD = time -avp -o $(TIMELOG)

SED_CMD = sed -i -e 's/_\(aw\|ar\|w\|r\|b\)_\(\|bits_\)/_\1/g'

.DEFAULT_GOAL = verilog

help:
	mill -i bridge[chisel].runMain $(FPGATOP) --help

$(TOP_V): $(SCALA_FILE)
	mkdir -p $(@D)
	$(TIME_CMD) mill -i bridge[chisel].runMain $(FPGATOP)
	$(SED_CMD) $@
	@git log -n 1 >> .__head__
	@git diff >> .__diff__
	@sed -i 's/^/\/\// ' .__head__
	@sed -i 's/^/\/\//' .__diff__
	@cat .__head__ .__diff__ $@ > .__out__
	@mv .__out__ $@
	@rm .__head__ .__diff__

verilog: $(TOP_V)

clean:
	rm -rf $(BUILD_DIR)

init:
	git submodule update --init
	cd rocket-chip && git submodule update --init cde hardfloat

bump:
	git submodule foreach "git fetch origin&&git checkout master&&git reset --hard origin/master"

bsp:
	mill -i mill.bsp.BSP/install

idea:
	mill -i mill.scalalib.GenIdea/idea

.PHONY: verilog clean help init bump bsp idea
