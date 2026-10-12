delete
from demand_instance_config
where (config_data -> '$.shielded' is null or config_data -> '$.shielded' = false)
  and (config_data -> '$.inBlacklisted' is null or config_data -> '$.inBlacklisted' = false);