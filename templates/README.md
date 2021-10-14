# Create Pipelines through Templates

In the following links you can see the documentation to generate pipelines through Templates

## CI Pipeline

Following the [doc](./docs/CreateCIPipelines.md "Create CI Pipeline") you can build a pipeline that:

- Builds an image.
- Deploys it on Nexus.
- Update project components.
- And displays the generated image.

## CD Pipeline

Following the [doc](./docs/CreateCDPipelines.md "Create CD Pipeline") you can build a pipeline that:

- Promotes an image of the Nexus from Low to High Environments.
- Update the project components and
- Deploys the chosen version of the application

## New Project

Following the [doc](./docs/CreateNewProject.md "Create New Project") you can configure a new project with all the steps you need to host pipelines:

- Create project
- Create git access credentials (these must be created *per* project)
- Update Jenkins configuration to sync the new project
