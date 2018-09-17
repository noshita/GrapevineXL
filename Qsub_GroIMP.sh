#!/bin/bash
#############################

# les directives PBS vont ici:

# Your job name (displayed by the queue)
#PBS -N Qsub_GroIMP_HL

# Specify the working directory
#PBS -d /home/jzhu/.Qsub_GroIMP.d/2016-01-14.15.22.33

# walltime (hh:mm::ss)
#PBS -l walltime=00:05:00

# Specify the number of nodes(nodes=) and the number of cores per nodes(ppn=) to be used
#PBS -l nodes=1:ppn=4

# fin des directives PBS
#############################

# modules cleaning
module purge
module add torque
module add java-jdk/oracle/64/1.7.0_03
module add GroIMP/1.5

# useful informations to print
echo "#############################" 
echo "User:" $USER >> /home/jzhu/.Qsub_GroIMP.d/2016-01-14.15.22.33/Qsub_GroIMP_info_run
echo "Date:" `date` >> /home/jzhu/.Qsub_GroIMP.d/2016-01-14.15.22.33/Qsub_GroIMP_info_run
echo "Host:" `hostname` >> /home/jzhu/.Qsub_GroIMP.d/2016-01-14.15.22.33/Qsub_GroIMP_info_run
echo "Directory:" `pwd` >> /home/jzhu/.Qsub_GroIMP.d/2016-01-14.15.22.33/Qsub_GroIMP_info_run
echo "PBS_JOBID:" $PBS_JOBID >> /home/jzhu/.Qsub_GroIMP.d/2016-01-14.15.22.33/Qsub_GroIMP_info_run
echo "PBS_O_WORKDIR:" $PBS_O_WORKDIR >> /home/jzhu/.Qsub_GroIMP.d/2016-01-14.15.22.33/Qsub_GroIMP_info_run
echo "PBS_NODEFILE: " `cat $PBS_NODEFILE | uniq` >> /home/jzhu/.Qsub_GroIMP.d/2016-01-14.15.22.33/Qsub_GroIMP_info_run 
echo "############################" >> /home/jzhu/.Qsub_GroIMP.d/2016-01-14.15.22.33/Qsub_GroIMP_info_run
echo "############################" >> /home/jzhu/.Qsub_GroIMP.d/2016-01-14.15.22.33/Qsub_GroIMP_info_run

java -Xmx800m -Djava.library.path=/cm/shared/contrib/apps/GroIMP/1.5/ext/ -classpath /cm/shared/contrib/apps/GroIMP/1.5/ext/gluegen-rt.jar:/cm/shared/contrib/apps/GroIMP/1.5/ext/jogl.jar:/cm/shared/contrib/apps/GroIMP/1.5/ext/jocl.jar:/cm/shared/contrib/apps/GroIMP/1.5/ext/commons-math3-3.5.jar -jar /cm/shared/contrib/apps/GroIMP/1.5/core.jar --headless --debug=ALL /home/jzhu/hlMainNormal.gsz 2>/home/jzhu/GroIMP_logfile.txt

