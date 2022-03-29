[This page](https://www.gravitywell.co.uk/insights/deploying-applications-to-ecs-fargate-with-aws-cdk/) has
many helpful details.

This project by default creates two instances of a hello service with two different endpoints.

## Infimia Samples 
### Installing Infima

From the [readme](https://github.com/awslabs/route53-infima) -- clone the project and use Maven to install it

git clone https://github.com/awslabs/route53-infima
cd route53-infima
mvn clean install -Dgpg.skip=true

### Sample 1

Looking at the stateful searching shuffle sharder, we can get the following combinations
from a set of endpoints, with a shard containing 2 endpoints with a max overlap of 1 with all
other shuffle shards.

12 endpoints: 66 shards (12 choose 2)
10 endpoints: 45 shards (10 choose 2)
8 endpoints: 28 shards (8 choose 2)

From Wolfram Alpha

50 endpoints: 1225
25 endpoints: 300

What is we go 3 endpoints per shard, max overlap of 2?


12 endpoints: 220 shards (12 choose 3)
10 endpoints: 120 shards (10 choose 3)
8 endpoints: 56 shards (8 choose 3)

50 - 19600
25 - 2300

### Sample 2

100,000 requests - endpoint distribution amongst a lattice of 12 endpoints:

{A=16786, B=16800, C=16503, D=16545, E=16665, F=16475, G=16623, H=16822, I=16841, J=16644, K=16809, L=16475}
