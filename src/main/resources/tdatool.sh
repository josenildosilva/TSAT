#!/usr/bin/env bash
#source /home/$USER/anaconda2/etc/profile.d/conda.sh;conda activate base; echo $CONDA_DEFAULT_ENV; conda execute -q target/classes/tdaInterface.py $1 $2 $3 $4 $5 $6 $7 --shouldConsolidate $8
echo $@
#source /home/$USER/anaconda2/etc/profile.d/conda.sh;conda activate py36; echo $CONDA_DEFAULT_ENV; python -u target/classes/tdaInterface.py $1 $2 $3 $4 $5 $6 $7 #--shouldConsolidate $8
#echo $PWD
#echo "source  /home/${USER}/.bashrc"
#echo ${tda}
source /home/${USER}/.tsat;tda $1 $2 $3 $4 $5 $6 $7 #--shouldConsolidate $8