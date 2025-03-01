# Our Scheme

Source codes of ProVerif, NS, VanetMobiSim in our proposed scheme.

## 1. ProVerif

### 1.1 Dependency

1) ProVerif 2.05, avaiable at https://bblanche.gitlabpages.inria.fr/proverif/

2) OPAM, avaiable at https://opam.ocaml.org/

### 1.2 Setps

1) Enter the BTDS-ProVerif2.05 file and use the BTDS.pv file:
order: proverif BTDS.pv

output: 
<img src=".\BTDS-ProVerif2.05\result.png" width = 40%>

## 2. Simulate the APD + PLR 

### 2.1 Dependency

1) ns 2.35, avaiable at: http://nchc.dl.sourceforge.net/project/nsnam/allinone/ns-allinone-2.35/ns-allinone-2.35.tar.gz

2) JDK, avaiable at: http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html

3) Ant, avaiable at: http://ant.apache.org/bindownload.cgi

4) VanetMobiSim, avaiable at:http://vanet.eurecom.fr/

### 2.2 Setps

1) Enter the BTDS-PLR-APD use vms_get_scen.xml to generate the simualtion scenario file:
order: java -jar VanetMobiSim.jar vms_get_scen.xml

output: vms_scen_file

2) Enter the BTDS-PLR-APD use the file of vms_scen_file to genrate .tcl file, that is, importing the scenario file <vms_scen_file> in ns.tcl:
order: ns ns.tcl

output: ns_trace.tr and ns_nam.nam

3) Analyse the average delay:
order: gawk -f awk_average_delay_RSU.awk ns_trace.tr

output: the final average delay

4) Analyse the packet loss:
order: gawk -f awk_Packet_Loss_RSU.awk ns_trace.tr

output: the final packet loss
