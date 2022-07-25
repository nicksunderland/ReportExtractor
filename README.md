## Bristol Heart Institute Report Extractor

###### Author: Nick Sunderland
###### Email: nicholas.p.sunderland@gmail.com


## Description
This programme extracts data from unstructured or semi-structured freetext echocardiogram reports.

## Input
The input needs to be a comma separated value (.csv) file with the following format:

<table>
  <tr> 	<th>id</th>		<th>datetime</th>		<th>report_text</th>					</tr>
  <tr>	<td>id_001</td>	<td>2020-01-01</td>		<td>example text...</td>				</tr>
  <tr>	<td>id_002</td>	<td>2021-01-01</td>		<td>SoV = 3.2cm (1.7cm/m).</td>		</tr>
  <tr>	<td>id_003</td>	<td>2022-01-01</td>		<td>StJ is 3.0cm, Asc Ao 3cm.</td>	</tr>
</table>

## Output
The output is a new .csv file created in the same directory as the the input file, named [input_file_name]_results.csv.
It is in the following format:

<table>
  <tr> 	<th>id</th>		<th>ao_sov_diam</th>		<th>ao_stj_diam</th>		<th>ao_asc_diam</th>		<th>ao_sov_ht_idx_diam</th>		<th>ao_stj_ht_idx_diam</th>		<th>ao_asc_ht_idx_diam</th>		</tr>
  <tr>	<td>id_001</td>	<td></td>	<td></td>	<td></td>	<td></td>	<td></td>	<td></td>	</tr>
  <tr>	<td>id_002</td>	<td>3.2</td>	<td></td>	<td></td>	<td>1.7</td>	<td></td>	<td></td>	</tr>
  <tr>	<td>id_003</td>	<td></td>	<td>3.0</td>	<td>3.0</td>	<td></td>	<td></td>	<td></td>	</tr>
</table>

## Future directions
Ultimately the aim is to incorporate all standard echocardiogram variables, then to branch out into other types of report such as cardiac MRI and CT coronary angiography. 