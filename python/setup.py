from os import path
import setuptools

pwd = path.abspath(path.dirname(__file__))
rdm_file = path.join(pwd, 'README.md')
with open(rdm_file, encoding='utf-8') as f:
    readme = f.read()

setuptools.setup(
    name='vertx-eventbus-bridge-starter',
    version='1.0.1',
    packages=['testeventbus'],
    author='Lin Gao',
    author_email='aoingl@gmail.com',
    url='https://github.com/gaol/test-eventbus-bridge',
    license='Apache Software License 2.0',
    description='Starter used to start an EventBus bridge for testing',
    long_description=readme,
    long_description_content_type='text/markdown'
)
